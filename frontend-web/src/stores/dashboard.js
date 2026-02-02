import { defineStore } from 'pinia';
import api from '@/services/api';

export const useDashboardStore = defineStore('dashboard', {
    state: () => ({
        stats: {
            total_despesas: 0,
            media_trimestral: 0,
            top_5_operadoras: [],
            top_5_estados: []
        },
        operadoras: {
            data: [],
            total: 0,
            page: 1,
            limit: 10
        },
        selectedOperadora: null,
        history: [], 
        historyMeta: {
            total: 0,
            page: 1,
            limit: 5,
            search: ''
        },
        loadingHistory: false,
        
        loadingStats: false,
        loadingOperadoras: false,
        error: null
    }),

    actions: {
        async fetchStats() {
            this.loadingStats = true;
            try {
                const response = await api.get('/estatisticas/');
                this.stats = response.data;
            } catch (err) {
                console.error(err);
            } finally {
                this.loadingStats = false;
            }
        },

        async fetchOperadoras(page = 1, search = '') {
            this.loadingOperadoras = true;
            this.operadoras.page = page;
            try {
                const params = { page, limit: this.operadoras.limit };
                if (search) params.search = search;

                const response = await api.get('/operadoras/', { params });
                this.operadoras.data = response.data.data;
                this.operadoras.total = response.data.total;
            } catch (err) {
                console.error(err);
            } finally {
                this.loadingOperadoras = false;
            }
        },

        async fetchHistory(cnpj, page = 1, search = '') {
            this.loadingHistory = true;
            this.history = [];

            const searchTerm = search !== undefined ? search : this.historyMeta.search;

            try {
                const response = await api.get(`/operadoras/${cnpj}/despesas`, {
                    params: {
                        page: page,
                        limit: this.historyMeta.limit,
                        search: searchTerm
                    }
                });
                
                this.history = response.data.data;
                this.historyMeta = {
                    total: response.data.total,
                    page: response.data.page,
                    limit: response.data.limit,
                    search: searchTerm
                };
            } catch (err) {
                console.error("Erro ao buscar histórico", err);
            } finally {
                this.loadingHistory = false;
            }
        },

        setSelectedOperadora(operadora) {
            this.selectedOperadora = operadora;
            if (operadora) {
                this.historyMeta.search = '';
                this.fetchHistory(operadora.cnpj, 1, '');
            }
        }
    }
});