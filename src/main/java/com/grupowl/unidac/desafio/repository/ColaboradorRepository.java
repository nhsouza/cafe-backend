package com.grupowl.unidac.desafio.repository;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.internal.build.AllowSysOut;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.grupowl.unidac.desafio.model.Colaborador;
import com.grupowl.unidac.desafio.model.Opcao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

@Repository
@Transactional
public class ColaboradorRepository {

	@PersistenceContext
	private EntityManager em;

	public List<Colaborador> readAll() {
		Query query = em.createNativeQuery("SELECT * FROM colaborador", Colaborador.class);
		return (List<Colaborador>) query.getResultList();
	}
	
	public Colaborador create(Colaborador colaborador) {
		Colaborador persistedColaborador = persistColaborador(colaborador);
		
		for (Opcao opcao : colaborador.getOpcoes()) {
			em.createNativeQuery("INSERT INTO opcao VALUES (NULL, ?, ?, ?)")
				.setParameter(1, persistedColaborador.getData())
				.setParameter(2, opcao.getNome())
				.setParameter(3, persistedColaborador.getId())
				.executeUpdate();
		}

 		return (Colaborador) em.createNativeQuery(
			"SELECT * FROM colaborador WHERE id = ?", Colaborador.class)
 				.setParameter(1, persistedColaborador.getId())
 				.getSingleResult();
	}

	private Colaborador persistColaborador(Colaborador colaborador) {
		em.createNativeQuery("INSERT INTO colaborador VALUES (NULL, ?, ?, ?)")
		.setParameter(1, colaborador.getCpf())
		.setParameter(2, colaborador.getData())
		.setParameter(3, colaborador.getNome())
		.executeUpdate();

		return (Colaborador) em.createNativeQuery(
				"SELECT * FROM colaborador WHERE cpf = ?", Colaborador.class)
					.setParameter(1, colaborador.getCpf()).getSingleResult();
	}

	public Colaborador read(Integer id) {
		return (Colaborador) em.createNativeQuery(
				"SELECT * FROM colaborador WHERE id = ?", Colaborador.class)
					.setParameter(1, id)
					.getSingleResult();
	}

	public void update(Integer id, Colaborador colaborador) {
		updateFields(id, colaborador);
		updateOptions(id, colaborador);
	}

	private void updateFields(Integer id, Colaborador atualizado) {
		Colaborador antigo = read(id);

		if (!antigo.getCpf().equalsIgnoreCase(atualizado.getCpf())) {
			em.createNativeQuery("UPDATE colaborador SET cpf = ? WHERE id = ?")
				.setParameter(1, atualizado.getCpf())
				.setParameter(2, id)
				.executeUpdate();
		}

		if (!antigo.getData().equals(atualizado.getData())) {
			em.createNativeQuery("UPDATE colaborador SET data_cafe = ? WHERE id = ?")
				.setParameter(1, atualizado.getData())
				.setParameter(2, id)
				.executeUpdate();
		}

		if (!antigo.getNome().equalsIgnoreCase(atualizado.getNome())) {
			em.createNativeQuery("UPDATE colaborador SET nome = ? WHERE id = ?")
				.setParameter(1, atualizado.getNome())
				.setParameter(2, id)
				.executeUpdate();
		}
	}

	private void updateOptions(Integer id, Colaborador atualizado) {
		Colaborador antigo = read(id);

		List<String> atualizadoOpcaoNomeList = new ArrayList<>();
		for (Opcao opcao : antigo.getOpcoes()) {
			atualizadoOpcaoNomeList.add(opcao.getNome());
		}

		List<String> novoOpcaoNomeList = new ArrayList<>();
		for (Opcao opcao : atualizado.getOpcoes()) {
			novoOpcaoNomeList.add(opcao.getNome());
		}

		for (String opcaoNome : novoOpcaoNomeList) {
			if (!atualizadoOpcaoNomeList.contains(opcaoNome)) {
				em.createNativeQuery("INSERT INTO opcao VALUES (NULL, ?, ?, ?)")
					.setParameter(1, antigo.getData())
					.setParameter(2, opcaoNome)
					.setParameter(3, id)
					.executeUpdate();
			}
		}

		for (String opcaoNome : atualizadoOpcaoNomeList) {
			if (!novoOpcaoNomeList.contains(opcaoNome)) {
				em.createNativeQuery("DELETE FROM opcao WHERE nome = ? AND data_cafe = ?")
					.setParameter(1, opcaoNome)
					.setParameter(2, antigo.getData())
					.executeUpdate();
			}
		}
	}

	public void delete(Integer id) {
		em.createNativeQuery("DELETE FROM colaborador WHERE id = ?")
			.setParameter(1, id)
			.executeUpdate();
	}
}