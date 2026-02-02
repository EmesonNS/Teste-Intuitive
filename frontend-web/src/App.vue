<script setup>
import { onMounted, ref, computed } from 'vue';
import { useDashboardStore } from '@/stores/dashboard';
import OperadoraModal from '@/components/OperadoraModal.vue';
import { 
  LayoutDashboard, Search, Building2, BarChart3, MapPin, 
  ChevronLeft, ChevronRight, TrendingUp, DollarSign, RefreshCw, FileText
} from 'lucide-vue-next';

import {
  Chart as ChartJS, Title, Tooltip, Legend, BarElement, CategoryScale, LinearScale, ArcElement
} from 'chart.js'
import { Bar, Doughnut } from 'vue-chartjs'

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend, ArcElement)

const store = useDashboardStore();
const searchQuery = ref('');

const formatCompact = (value) => {
  return new Intl.NumberFormat('pt-BR', { notation: "compact", compactDisplay: "short" }).format(value);
};

const formatCNPJ = (value) => {
  if (!value) return '';
  return value.replace(/^(\d{2})(\d{3})(\d{3})(\d{4})(\d{2})/, "$1.$2.$3/$4-$5");
};

const chartColors = ['#0ea5e9', '#10b981', '#f59e0b', '#a855f7', '#ec4899'];

const barChartData = computed(() => ({
  labels: store.stats.top_5_operadoras.map(op => {
      const name = op.razao_social;
      return name.length > 15 ? name.substring(0, 15) + '...' : name;
  }),
  datasets: [{ 
    label: 'Total',
    data: store.stats.top_5_operadoras.map(op => op.valor_total),
    backgroundColor: chartColors,
    borderRadius: 4,
    barPercentage: 0.7,
  }]
}));

const donutChartData = computed(() => ({
  labels: store.stats.top_5_estados.map(uf => uf.uf),
  datasets: [{
    data: store.stats.top_5_estados.map(uf => uf.total),
    backgroundColor: chartColors,
    borderWidth: 0,
    hoverOffset: 4
  }]
}));

const barOptions = {
  indexAxis: 'y', 
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { display: false },
    tooltip: { 
        backgroundColor: '#1e293b',
        padding: 12,
        cornerRadius: 8
    }
  },
  scales: {
    x: {
        grid: { display: false },
        ticks: { 
            display: true,
            font: { size: 10, family: 'Inter' },
            callback: (val) => formatCompact(val)
        },
        border: { display: false }
    },
    y: {
        grid: { display: false },
        ticks: { font: { size: 11, family: 'Inter', weight: '500' } },
        border: { display: false }
    }
  }
};

const donutOptions = {
  responsive: true,
  maintainAspectRatio: false,
  layout: { padding: 20 },
  plugins: {
    legend: { 
        display: true, 
        position: 'bottom',
        labels: {
            usePointStyle: true,
            padding: 20,
            font: { family: 'Inter', size: 11 },
            boxWidth: 8
        }
    }
  },
  cutout: '65%'
};

let timeout = null;
const handleSearch = () => {
  clearTimeout(timeout);
  timeout = setTimeout(() => {
    store.fetchOperadoras(1, searchQuery.value);
  }, 500);
};

const openDetails = (operadora) => {
    store.setSelectedOperadora(operadora);
};

onMounted(() => {
  store.fetchStats();
  store.fetchOperadoras();
});
</script>

<template>
  <div class="min-h-screen bg-[#F8F9FA] font-sans text-slate-800">
    <OperadoraModal />

    <nav class="bg-white border-b border-gray-200 sticky top-0 z-30">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between h-16">
          <div class="flex items-center gap-3">
            <div class="bg-intuitive-500 p-2 rounded-lg text-white shadow-md shadow-intuitive-500/30">
              <LayoutDashboard size="22" stroke-width="2.5" />
            </div>
            <div>
                <h1 class="font-bold text-lg text-slate-900 leading-none tracking-tight">ANS Dashboard</h1>
                <span class="text-xs text-slate-500 font-medium">Painel de Operadoras de Saúde</span>
            </div>
          </div>
          <div class="flex items-center">
            <button 
                class="flex items-center gap-2 px-4 py-2 bg-white border border-gray-200 rounded-lg text-sm font-semibold text-slate-600 hover:bg-gray-50 hover:text-intuitive-600 transition-all shadow-sm active:scale-95"
                @click="store.fetchStats(); store.fetchOperadoras()"
            >
              <RefreshCw size="16" :class="{ 'animate-spin': store.loadingStats }" />
              Atualizar
            </button>
          </div>
        </div>
      </div>
    </nav>

    <main class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div class="bg-white p-6 rounded-xl shadow-[0_2px_15px_-3px_rgba(0,0,0,0.03)] border border-slate-100 flex items-start justify-between">
          <div>
            <p class="text-xs font-bold uppercase tracking-wider text-slate-400 mb-1">Total de Despesas</p>
            <h3 class="text-3xl font-bold text-slate-900">{{ formatCompact(store.stats.total_despesas) }}</h3>
            <p class="text-xs text-slate-500 mt-2 font-medium">Soma de todas as despesas registradas</p>
          </div>
          <div class="p-3 bg-blue-50 rounded-xl text-blue-600">
            <DollarSign size="24" stroke-width="2.5" />
          </div>
        </div>

        <div class="bg-white p-6 rounded-xl shadow-[0_2px_15px_-3px_rgba(0,0,0,0.03)] border border-slate-100 flex items-start justify-between">
          <div>
            <p class="text-xs font-bold uppercase tracking-wider text-slate-400 mb-1">Média Trimestral</p>
            <h3 class="text-3xl font-bold text-slate-900">{{ formatCompact(store.stats.media_trimestral) }}</h3>
            <div class="mt-2 flex items-center gap-2">
                <span class="text-xs font-bold text-green-600 bg-green-50 px-1.5 py-0.5 rounded">+5.2%</span>
                <span class="text-xs text-slate-400">vs. trimestre anterior</span>
            </div>
          </div>
          <div class="p-3 bg-sky-50 rounded-xl text-sky-600">
            <TrendingUp size="24" stroke-width="2.5" />
          </div>
        </div>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div class="bg-white p-6 rounded-xl shadow-[0_2px_15px_-3px_rgba(0,0,0,0.03)] border border-slate-100">
          <h3 class="text-base font-bold text-slate-800 mb-6">Top 5 Operadoras por Despesas</h3>
          <div class="h-64 relative">
            <Bar v-if="!store.loadingStats" :data="barChartData" :options="barOptions" />
          </div>
        </div>

        <div class="bg-white p-6 rounded-xl shadow-[0_2px_15px_-3px_rgba(0,0,0,0.03)] border border-slate-100">
          <h3 class="text-base font-bold text-slate-800 mb-6">Despesas por Estado</h3>
          <div class="h-64 relative flex justify-center">
            <Doughnut v-if="!store.loadingStats" :data="donutChartData" :options="donutOptions" />
          </div>
        </div>
      </div>

      <div class="bg-white rounded-xl shadow-[0_2px_15px_-3px_rgba(0,0,0,0.03)] border border-slate-100 overflow-hidden">
        <div class="p-6 border-b border-gray-100 flex flex-col sm:flex-row justify-between items-center gap-4 bg-white">
          <h3 class="text-lg font-bold text-slate-800">Operadoras de Saúde</h3>
          
          <div class="relative w-full sm:w-96">
            <input 
              v-model="searchQuery"
              @input="handleSearch"
              type="text" 
              placeholder="Buscar por razão social ou CNPJ..." 
              class="w-full pl-10 pr-4 py-2.5 border border-gray-200 bg-gray-50 rounded-lg focus:outline-none focus:ring-2 focus:ring-intuitive-500 focus:bg-white transition-all text-sm text-slate-700 placeholder:text-slate-400"
            >
            <Search class="absolute left-3 top-3 text-slate-400" size="18" />
          </div>
        </div>

        <div class="overflow-x-auto">
          <table class="w-full text-left text-sm text-slate-600">
            <thead class="bg-gray-50/50 text-xs font-semibold text-slate-500 border-b border-gray-100">
              <tr>
                <th class="px-6 py-4 font-semibold">Operadora</th>
                <th class="px-6 py-4 font-semibold">CNPJ</th>
                <th class="px-6 py-4 font-semibold">Modalidade</th>
                <th class="px-6 py-4 font-semibold">UF</th>
                <th class="px-6 py-4 font-semibold text-right">Ações</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-50">
              <tr v-if="store.loadingOperadoras">
                <td colspan="5" class="px-6 py-16 text-center text-slate-400">Carregando dados...</td>
              </tr>
              <tr 
                v-for="op in store.operadoras.data" 
                :key="op.registro_ans" 
                @click="openDetails(op)"
                class="group hover:bg-slate-50 cursor-pointer transition-colors duration-150"
              >
                <td class="px-6 py-4">
                  <div class="flex items-center gap-3">
                    <div class="w-10 h-10 rounded-lg bg-intuitive-50 text-intuitive-600 flex items-center justify-center shrink-0 border border-intuitive-100">
                      <Building2 size="20" stroke-width="1.5" />
                    </div>
                    <div>
                        <div class="font-semibold text-slate-900 line-clamp-1" :title="op.razao_social">{{ op.razao_social }}</div>
                        <div class="text-xs text-slate-400 font-medium mt-0.5">ANS: {{ op.registro_ans }}</div>
                    </div>
                  </div>
                </td>
                
                <td class="px-6 py-4 font-mono text-xs text-slate-500">{{ formatCNPJ(op.cnpj) }}</td>
                
                <td class="px-6 py-4">
                  <span class="px-3 py-1 rounded-full text-xs font-medium bg-sky-50 text-sky-700 border border-sky-100 whitespace-nowrap">
                    {{ op.modalidade }}
                  </span>
                </td>
                
                <td class="px-6 py-4 text-slate-500 font-medium flex items-center gap-1">
                    <MapPin size="14" /> {{ op.uf }}
                </td>
                
                <td class="px-6 py-4 text-right">
                  <button 
                    class="opacity-0 group-hover:opacity-100 transition-all duration-200 inline-flex items-center gap-2 px-3 py-1.5 rounded-lg text-xs font-semibold text-slate-500 hover:text-green-700 hover:bg-green-50 border border-transparent hover:border-green-200"
                    @click.stop="openDetails(op)"
                  >
                    <FileText size="14" />
                    Detalhes
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div class="p-4 border-t border-gray-100 flex justify-end items-center bg-white gap-3">
            <button 
              @click="store.fetchOperadoras(store.operadoras.page - 1, searchQuery)"
              :disabled="store.operadoras.page === 1"
              class="w-8 h-8 flex items-center justify-center rounded-md border border-gray-200 text-slate-600 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed transition-all shadow-sm"
            >
              <ChevronLeft size="16" />
            </button>
            
            <span class="text-sm font-medium text-slate-600">
                Página <span class="text-slate-900 font-bold">{{ store.operadoras.page }}</span> de {{ Math.ceil(store.operadoras.total / store.operadoras.limit) }}
            </span>

            <button 
              @click="store.fetchOperadoras(store.operadoras.page + 1, searchQuery)"
              :disabled="store.operadoras.page * store.operadoras.limit >= store.operadoras.total"
              class="w-8 h-8 flex items-center justify-center rounded-md border border-gray-200 text-slate-600 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed transition-all shadow-sm"
            >
              <ChevronRight size="16" />
            </button>
        </div>
      </div>

    </main>
  </div>
</template>