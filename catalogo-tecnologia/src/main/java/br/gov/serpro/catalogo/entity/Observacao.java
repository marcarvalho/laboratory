package br.gov.serpro.catalogo.entity;

import static javax.persistence.GenerationType.SEQUENCE;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@NamedQueries({ @NamedQuery(name = Observacao.OBSERVACOES_NA_FASE, query = " from Observacao a where (a.fase.id = :fase) ORDER BY a.data DESC") })
@Entity
public class Observacao {

	public static final String OBSERVACOES_NA_FASE = "OBSERVACOES_NA_FASE";

	@Id
	@GeneratedValue(strategy = SEQUENCE)
	private Long id;

	@ManyToOne
    @JoinColumn(nullable=false, updatable=false)
	private Fase fase;

	private String observacao;

	@Temporal(TemporalType.TIMESTAMP)
	private Date data;

	@ManyToOne 
    @JoinColumn(nullable=false, updatable=false)
	private User usuario;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Fase getFase() {
		return fase;
	}

	public void setFase(Fase fase) {
		this.fase = fase;
	}

	public String getObservacao() {
		return observacao;
	}

	public void setObservacao(String observacao) {
		this.observacao = observacao;
	}

	public Date getData() {
		return data;
	}

	public void setData(Date data) {
		this.data = data;
	}

	public User getUsuario() {
		return usuario;
	}

	public void setUsuario(User usuario) {
		this.usuario = usuario;
	}

	
	
}
