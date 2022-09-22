package com.carteira.services.impl;

import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.carteira.exception.ErroAutenticacao;
import com.carteira.exception.RegraNegocioException;
import com.carteira.model.entity.Usuario;
import com.carteira.repositories.UsuarioRepository;
import com.carteira.services.UsuarioService;

@Service
public class UsuarioServiceImpl implements UsuarioService {

	@Autowired
	private UsuarioRepository repository;
	
	@Override
	public Usuario autenticar(String email, String senha) {
		Optional<Usuario> usuario = repository.findByEmail(email);
		
		if(!usuario.isPresent()) { 
			throw new ErroAutenticacao("Usuário não encontrado.");
		}
		
		if (!usuario.get().getSenha().equals(senha)) { 
			throw new ErroAutenticacao("Senha incorreta.");
		}
		
		return usuario.get();
	}

	@Override
	@Transactional //quando tá tendo manipulação dentro do banco
	public Usuario salvarUsuario(Usuario usuario) {
		validarEmail(usuario.getEmail());
		return repository.save(usuario);
	}

	@Override
	public void validarEmail(String email) {
		boolean existe = repository.existsByEmail(email);
		if (existe) { 
			throw new RegraNegocioException
			("Já existe um usuário cadastrado com esse e-mail.");
		}
		
	}

	@Override
	public Optional<Usuario> buscarPorId(Long id) {
		return repository.findById(id);
	}

}
