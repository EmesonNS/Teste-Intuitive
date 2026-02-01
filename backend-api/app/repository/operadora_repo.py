from sqlalchemy.orm import Session
from sqlalchemy import or_, func, text
from app.models.operadora import Operadora, DespesaDetalhada, DespesaAgregada

class OperadoraRepository:

    @staticmethod
    def get_all(db: Session, skip: int = 0, limit: int = 10, search: str = None):
        query = db.query(Operadora)

        if search:
            search_filter = f"%{search}%"
            query = query.filter(
                or_(
                    Operadora.razao_social.ilike(search_filter),
                    Operadora.cnpj.ilike(search_filter)
                )
            )

        total = query.count()
        result = query.order_by(Operadora.razao_social).offset(skip).limit(limit).all()
        return result, total
        
    @staticmethod
    def get_by_cnpj(db: Session, cnpj: str):
        clean_cnpj = cnpj.replace(".", "").replace("/", "").replace("-", "")
        return db.query(Operadora).filter(Operadora.cnpj == clean_cnpj).first()
    
    @staticmethod
    def get_despesas_by_cnpj(db: Session, cnpj: str, skip: int = 0, limit: int = 10):
        clean_cnpj = cnpj.replace(".", "").replace("/", "").replace("-", "")
        operadora = db.query(Operadora).filter(Operadora.cnpj == clean_cnpj).first()
        if operadora:
            query = db.query(DespesaDetalhada).filter(
                DespesaDetalhada.registro_ans == operadora.registro_ans
            )

            total = query.count()

            data = query.order_by(
                DespesaDetalhada.ano.desc(),
                DespesaDetalhada.trimestre.desc()
            ).offset(skip).limit(limit).all()

            return data, total
        
        return None, 0
    
    @staticmethod
    def get_statistics(db: Session):
        total_geral = db.query(func.sum(DespesaAgregada.valor_total)).scalar() or 0
        
        count_ops = db.query(func.count(DespesaAgregada.razao_social)).scalar() or 1
        media_geral = total_geral / count_ops if count_ops > 0 else 0

        top_5 = db.query(
            DespesaAgregada.razao_social,
            DespesaAgregada.valor_total,
            Operadora.registro_ans
        ).outerjoin(
            Operadora,
            DespesaAgregada.razao_social == Operadora.razao_social
        ).order_by(DespesaAgregada.valor_total.desc()).limit(5).all()
        
        top_estados_sql = text("""
            SELECT uf, SUM(valor_total) as total 
            FROM despesas_agregadas 
            GROUP BY uf 
            ORDER BY total DESC 
            LIMIT 5
        """)
        top_estados = db.execute(top_estados_sql).fetchall()

        return {
            "total_despesas": float(total_geral),
            "media_trimestral": float(media_geral), # Simplificação para o dashboard
            "top_5_operadoras": [
                {
                    "registro_ans": row.registro_ans if row.registro_ans else "N/D", 
                    "razao_social": row.razao_social, 
                    "valor_total": float(row.valor_total)
                } 
                for row in top_5
            ],
            "top_5_estados": [{"uf": row[0], "total": float(row[1])} for row in top_estados]
        }