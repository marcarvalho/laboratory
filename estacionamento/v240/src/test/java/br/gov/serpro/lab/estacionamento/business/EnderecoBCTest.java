

package br.gov.serpro.lab.estacionamento.business;

import static org.junit.Assert.*;
import java.util.*;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import br.gov.frameworkdemoiselle.junit.DemoiselleRunner;
import br.gov.serpro.lab.estacionamento.domain.Endereco;
import br.gov.serpro.lab.estacionamento.business.EnderecoBC;

@RunWith(DemoiselleRunner.class)
public class EnderecoBCTest {

    @Inject
	private EnderecoBC enderecoBC;
	
	@Before
	public void before() {
		for (Endereco endereco : enderecoBC.findAll()) {
			enderecoBC.delete(endereco.getCodigo());
		}
	}	
	
	
	@Test
	public void testInsert() {
				
		Endereco endereco = new Endereco("logradouro","cidade","UF","00000-000",null);
		enderecoBC.insert(endereco);
		List<Endereco> listOfEndereco = enderecoBC.findAll();
		assertNotNull(listOfEndereco);
		assertEquals(1, listOfEndereco.size());
	}	
	
	@Test
	public void testDelete() {
		
		Endereco endereco = new Endereco("logradouro","cidade","UF","00000-000",null);
		enderecoBC.insert(endereco);
		
		List<Endereco> listOfEndereco = enderecoBC.findAll();
		assertNotNull(listOfEndereco);
		assertEquals(1, listOfEndereco.size());
		
		enderecoBC.delete(endereco.getCodigo());
		listOfEndereco = enderecoBC.findAll();
		assertEquals(0, listOfEndereco.size());
	}
	
	@Test
	public void testUpdate() {
		
		Endereco endereco = new Endereco("logradouro","cidade","UF","00000-000",null);
		enderecoBC.insert(endereco);
		
		List<Endereco> listOfEndereco = enderecoBC.findAll();
		Endereco endereco2 = (Endereco)listOfEndereco.get(0);
		assertNotNull(listOfEndereco);

		 endereco2.setLogradouro("novo valor");
		enderecoBC.update(endereco2);
		
		listOfEndereco = enderecoBC.findAll();
		Endereco endereco3 = (Endereco)listOfEndereco.get(0);
		
		assertEquals("novo valor", endereco3.getLogradouro());
	}

}