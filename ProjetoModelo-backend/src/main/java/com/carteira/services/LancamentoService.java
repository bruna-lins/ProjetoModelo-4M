package com.carteira.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.carteira.model.entity.Lancamento;
import com.carteira.model.enums.StatusLancamento;

public interface LancamentoService {

	Lancamento salvar(Lancamento lancamento);
	
	Lancamento atualizar(Lancamento lancamento);
	
	void deletar(Lancamento lancamento);
	
	List<Lancamento> buscar(Lancamento lancamentoSearch);
	
	void atualizarStatus(Lancamento lancamento, StatusLancamento status);
	
	void validar(Lancamento lancamento);
	
	Optional<Lancamento> buscarPorId(Long id);
	
	BigDecimal obterSaldoPorUsuario(Long id);
}
