import os
from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    PROJECT_NAME: str = "Intuitivr Care API"
    API_V1_STR: str = "/api"

    DATABASE_URL: str = os.getenv("DATABASE_URL", "")

settings = Settings()