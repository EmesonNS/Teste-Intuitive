from pydantic import BaseModel
from typing import List, Dict, Any

class TopOperadora(BaseModel):
    registro_ans: str
    razao_social: str
    valor_total: float

class StatsResponse(BaseModel):
    total_despesas: float
    media_trimestral: float 
    top_5_operadoras: List[TopOperadora]
    top_5_estados: List[Dict[str, Any]]