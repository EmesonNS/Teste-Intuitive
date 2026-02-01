from sqlalchemy import Column, String, Integer, Numeric, Text, ForeignKey, TIMESTAMP
from sqlalchemy.orm import relationship
from app.db.base import Base

class Operadora(Base):
    __tablename__ = "operadoras"

    registro_ans = Column(String, primary_key=True, index=True)
    cnpj = Column(String, index=True)
    razao_social = Column(String, index=True)
    modalidade = Column(String)
    uf = Column(String)

    despesas = relationship("DespesaDetalhada", back_populates="operadora")


class DespesaDetalhada(Base):
    __tablename__ = "despesas_detalhadas"

    id = Column(Integer, primary_key=True, index=True)
    registro_ans = Column(String, ForeignKey("operadoras.registro_ans"))
    trimestre = Column(Integer)
    ano = Column(Integer)
    valor = Column(Numeric(18, 2))
    descricao = Column(Text)
    data_carga = Column(TIMESTAMP)

    operadora = relationship("Operadora", back_populates="despesas")


class DespesaAgregada(Base):
    __tablename__ = "despesas_agregadas"

    razao_social = Column(String, primary_key=True)
    uf = Column(String, primary_key=True)
    valor_total = Column(Numeric(18, 2))
    media_trimestral = Column(Numeric(18, 2))
    desvio_padrao = Column(Numeric(18, 2))
    qtd_registros = Column(Integer)