package com.carteira.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.carteira.dto.AtualizarStatusDTO;
import com.carteira.dto.LancamentoDTO;
import com.carteira.exception.RegraNegocioException;
import com.carteira.model.entity.Lancamento;
import com.carteira.model.entity.Usuario;
import com.carteira.model.enums.StatusLancamento;
import com.carteira.model.enums.TipoLancamento;
import com.carteira.services.LancamentoService;
import com.carteira.services.UsuarioService;

@RestController
@RequestMapping("/api/lancamentos")
public class LancamentoController {

	@Autowired
	private LancamentoService service;
	
	@Autowired
	private UsuarioService usuarioService;
	
	@PostMapping
	public ResponseEntity salvar(@RequestBody LancamentoDTO dto ) { 
		try { 
			Lancamento entidade = converter(dto);
			entidade = service.salvar(entidade);
			return new ResponseEntity(entidade, HttpStatus.CREATED);
		} catch (RegraNegocioException e){
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}
	
	@PutMapping("{id}")
	public ResponseEntity atualizar(@PathVariable Long id, @RequestBody LancamentoDTO dto) {
		return service.buscarPorId(id).map(entity -> {
			try { 
				Lancamento lancamento = converter(dto);
				lancamento.setId(entity.getId());
				service.atualizar(lancamento);
				return ResponseEntity.ok(lancamento);
			} catch (RegraNegocioException e) { 
				return ResponseEntity.badRequest().body(e.getMessage());
			}
		}).orElseGet(() -> new ResponseEntity<String>("Lançamento não encontrado"
				+ "no banco de dados", HttpStatus.BAD_REQUEST));
	}
	
	@PutMapping("{id}/atualiza-status")
	public ResponseEntity atualizarStatus(@PathVariable("id") Long id, @RequestBody AtualizarStatusDTO dto) { 
		return service.buscarPorId(id).map(entity -> {
			StatusLancamento statusSelecionado = StatusLancamento.valueOf(dto.getStatus());
			
			if(statusSelecionado == null) { 
				return ResponseEntity.badRequest().body("Não foi possível atualizar o lançamento,"
						+ "mande um status valido.");
			}
			
			try { 
				entity.setStatus(statusSelecionado);
				service.atualizar(entity);
				return ResponseEntity.ok(entity);
			} catch (RegraNegocioException e){ 
				return ResponseEntity.badRequest().body(e.getMessage());
			}
		}).orElseGet(() -> new ResponseEntity("Lançamento não encontrado",
				HttpStatus.BAD_REQUEST));
	}
	
	@DeleteMapping("{id}")
	public ResponseEntity deletar(@PathVariable("id") Long id) { 
		return service.buscarPorId(id).map(entity -> { 
			service.deletar(entity);
			return new ResponseEntity(HttpStatus.NO_CONTENT);
		}).orElseGet(() -> new ResponseEntity<String>("Lançamento não encontrado", 
				HttpStatus.BAD_REQUEST));
	};
	
	@GetMapping
	public ResponseEntity buscar(
			@RequestParam(value = "descricao", required = false) String descricao,
			@RequestParam(value = "mes", required = false) Integer mes,
			@RequestParam(value = "ano", required = false) Integer ano,
			@RequestParam("usuario") Long idUsuario
			) { 
		
		Lancamento lancamentoSearch = new Lancamento();
		lancamentoSearch.setDescricao(descricao);
		lancamentoSearch.setMes(mes);
		lancamentoSearch.setAno(ano);
		
		Optional<Usuario> usuario = usuarioService.buscarPorId(idUsuario);
		if(!usuario.isPresent()) { 
			return ResponseEntity.badRequest().body("Usuário não encontrado.");
		} else { 
			lancamentoSearch.setUsuario(usuario.get());
		}
		
		List<Lancamento> lancamento = service.buscar(lancamentoSearch);
		return ResponseEntity.ok(lancamento);
	}
	
	private Lancamento converter(LancamentoDTO dto) {
		Lancamento lancamento = new Lancamento();
		lancamento.setId(dto.getId());
		lancamento.setDescricao(dto.getDescricao());
		lancamento.setAno(dto.getAno());
		lancamento.setMes(dto.getMes());
		lancamento.setValor(dto.getValor());
		
		Usuario usuario = usuarioService.buscarPorId(dto.getUsuario())
				.orElseThrow(() -> new RegraNegocioException
						("Usuário com id " + dto.getUsuario() + " não encontrado"));
		
		lancamento.setUsuario(usuario);
		
		if(dto.getTipo() != null) { 
			lancamento.setTipo(TipoLancamento.valueOf(dto.getTipo()));
		}
		
		if(dto.getStatus() != null) { 
			lancamento.setStatus(StatusLancamento.valueOf(dto.getStatus()));
		}
		
		return lancamento;
	}
	
	
}
