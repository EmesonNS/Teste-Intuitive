<script setup>
import { useDashboardStore } from '@/stores/dashboard';
import { X, FileText, MapPin, ShieldCheck, Calendar, Building2, ChevronLeft, ChevronRight, Search } from 'lucide-vue-next';
import { ref } from 'vue';

const store = useDashboardStore();
const historySearch = ref('');

const close = () => {
  store.selectedOperadora = null;
};

const formatCurrency = (value) => {
  return new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(value);
};

const formatDate = (dateString) => {
    if(!dateString) return '-';
    const date = new Date(dateString);
    return new Intl.DateTimeFormat('pt-BR', { day: '2-digit', month: 'short', year: 'numeric' }).format(date);
}

const formatCNPJ = (value) => {
  if (!value) return '';
  return value.replace(/^(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})/, "$1.$2.$3/$4-$5");
};

let searchTimeout = null;
const handleHistorySearch = () => {
    clearTimeout(searchTimeout);
    searchTimeout = setTimeout(() => {
        store.fetchHistory(store.selectedOperadora.cnpj, 1, historySearch.value);
    }, 500);
};

const changePage = (newPage) => {
    if (newPage < 1 || newPage > Math.ceil(store.historyMeta.total / store.historyMeta.limit)) return;
    store.fetchHistory(store.selectedOperadora.cnpj, newPage); 
};
</script>

<template>
  <div v-if="store.selectedOperadora" class="fixed inset-0 z-50 flex items-center justify-center p-4 sm:p-6 font-sans">
    <div class="absolute inset-0 bg-slate-900/40 backdrop-blur-sm transition-opacity" @click="close"></div>

    <div class="relative bg-white rounded-xl shadow-2xl w-full max-w-5xl max-h-[90vh] flex flex-col overflow-hidden animate-in fade-in zoom-in duration-200">
      
      <div class="p-8 border-b border-gray-100 flex justify-between items-start shrink-0 bg-white">
        <div class="flex gap-5">
          <div class="w-14 h-14 rounded-xl bg-intuitive-100 text-intuitive-600 flex items-center justify-center shrink-0">
            <Building2 size="28" stroke-width="1.5" />
          </div>
          
          <div>
            <h3 class="text-2xl font-bold text-slate-900 leading-none mb-3">{{ store.selectedOperadora.razao_social }}</h3>
            <div class="flex flex-wrap items-center gap-3 text-sm">
              <span class="bg-slate-800 text-white px-3 py-1 rounded-md font-medium text-xs">
                ANS: {{ store.selectedOperadora.registro_ans }}
              </span>
              <span class="text-slate-500 font-mono">CNPJ: {{ formatCNPJ(store.selectedOperadora.cnpj) }}</span>
            </div>
            
            <div class="flex items-center gap-4 mt-4 text-sm text-slate-500">
                <span class="flex items-center gap-1.5">
                    <FileText size="16" class="text-slate-400"/> {{ store.selectedOperadora.modalidade }}
                </span>
                <span class="flex items-center gap-1.5">
                    <MapPin size="16" class="text-slate-400"/> {{ store.selectedOperadora.uf }}
                </span>
            </div>
          </div>
        </div>

        <button @click="close" class="text-slate-400 hover:text-slate-600 transition-colors p-2 hover:bg-slate-50 rounded-full">
          <X size="24" />
        </button>
      </div>

      <div class="px-8 pt-8 pb-4 bg-white shrink-0 flex flex-col sm:flex-row justify-between items-center gap-4">
        
        <h4 class="text-base font-bold text-slate-800 flex items-center gap-2">
            <Calendar size="20" class="text-intuitive-500" />
            Histórico de Despesas
        </h4>

        <div class="relative w-full sm:w-64">
            <input 
              v-model="historySearch"
              @input="handleHistorySearch"
              type="text" 
              placeholder="Filtrar despesa por descrição..." 
              class="w-full pl-9 pr-4 py-2 border border-gray-200 bg-gray-50 rounded-lg focus:outline-none focus:ring-2 focus:ring-intuitive-500 focus:bg-white transition-all text-xs text-slate-700 placeholder:text-slate-400"
            >
            <Search class="absolute left-3 top-2.5 text-slate-400" size="14" />
        </div>

      </div>

      <div class="px-8 pb-4 flex-1 overflow-y-auto">
        <div class="rounded-lg border border-gray-100 overflow-hidden">
            <table class="w-full text-sm text-left">
                <thead class="bg-gray-50 text-slate-500 font-semibold uppercase text-xs tracking-wider sticky top-0 z-10">
                    <tr>
                        <th class="px-6 py-4">Período</th>
                        <th class="px-6 py-4">Descrição</th>
                        <th class="px-6 py-4">Data Carga</th>
                        <th class="px-6 py-4 text-right">Valor</th>
                    </tr>
                </thead>
                <tbody class="divide-y divide-gray-100 bg-white">
                    <tr v-if="store.loadingHistory">
                        <td colspan="4" class="px-6 py-12 text-center text-slate-400">Carregando histórico...</td>
                    </tr>
                    <tr v-else-if="store.history.length === 0">
                        <td colspan="4" class="px-6 py-12 text-center text-slate-400">Nenhum registro encontrado.</td>
                    </tr>
                    <tr v-for="(item, index) in store.history" :key="index" class="hover:bg-gray-50 transition-colors">
                        <td class="px-6 py-4 font-medium text-slate-700 whitespace-nowrap">
                            {{ item.trimestre }}º Tri / {{ item.ano }}
                        </td>
                        <td class="px-6 py-4 text-slate-600 max-w-md truncate" :title="item.descricao">
                            {{ item.descricao }}
                        </td>
                        <td class="px-6 py-4 text-slate-500 whitespace-nowrap">
                            {{ formatDate(item.data_carga) }}
                        </td>
                        <td class="px-6 py-4 text-right font-bold text-slate-900 whitespace-nowrap">
                            {{ formatCurrency(item.valor) }}
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
      </div>

      <div class="px-8 py-4 border-t border-gray-100 bg-gray-50 shrink-0 flex justify-between items-center">
          <span class="text-xs text-slate-500">
             Mostrando {{ store.history.length }} registros
          </span>
          
          <div class="flex items-center gap-2">
             <button 
                @click="changePage(store.historyMeta.page - 1)"
                :disabled="store.historyMeta.page === 1 || store.loadingHistory"
                class="p-1.5 rounded-md border border-gray-200 bg-white hover:bg-gray-50 disabled:opacity-50 transition-colors"
             >
                <ChevronLeft size="16" />
             </button>
             
             <span class="text-xs font-medium text-slate-600">
                Página {{ store.historyMeta.page }} de {{ Math.ceil(store.historyMeta.total / store.historyMeta.limit) }}
             </span>

             <button 
                @click="changePage(store.historyMeta.page + 1)"
                :disabled="store.historyMeta.page * store.historyMeta.limit >= store.historyMeta.total || store.loadingHistory"
                class="p-1.5 rounded-md border border-gray-200 bg-white hover:bg-gray-50 disabled:opacity-50 transition-colors"
             >
                <ChevronRight size="16" />
             </button>
          </div>
      </div>

    </div>
  </div>
</template>