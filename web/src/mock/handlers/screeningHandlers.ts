// @ts-nocheck

export const screeningExactHandlers = {
  'GET /api/v1/web/screening/overview/': async () => {
    return {
      status: 200,
      data: {
        updatedAt: new Date().toLocaleString(),
        statCards: [
          { title: '累计筛查总人数', value: 12450, color: '#409EFF', icon: 'User', trend: 5.2 },
          { title: '检出高危/阳性', value: 342, color: '#f56c6c', icon: 'WarnTriangleFilled', trend: 1.5, suffix: '例' },
          { title: '基因突变携带率', value: 2.8, color: '#e6a23c', icon: 'TrendCharts', trend: -0.3, suffix: '%' },
          { title: 'AI 干预采纳数', value: 890, color: '#67c23a', icon: 'MagicStick', trend: 12.4, suffix: '次' }
        ],
        riskDistribution: [
          { level: '极高', count: 85, percentage: 1, color: '#f56c6c' },
          { level: '高', count: 257, percentage: 2, color: '#e6a23c' },
          { level: '中', count: 420, percentage: 3, color: '#e6a23c' },
          { level: '低', count: 11688, percentage: 94, color: '#67c23a' }
        ],
        topGenes: [
          { name: 'ATP7B', desc: '肝豆状核变性', percentage: 88 },
          { name: 'SLC25A13', desc: 'Citrin缺乏症', percentage: 82 },
          { name: 'ABCB11', desc: 'PFIC2', percentage: 76 },
          { name: 'ABCB4', desc: 'PFIC3', percentage: 70 },
          { name: 'JAG1', desc: 'Alagille综合征', percentage: 64 },
          { name: 'SLC10A1', desc: 'NTCP缺乏症', percentage: 58 },
          { name: 'LIPA', desc: '溶酶体酸性脂肪酶缺乏', percentage: 52 },
          { name: 'G6PC', desc: '糖原累积病I型', percentage: 45 }
        ],
        aiEfficiency: {
          diagnosisMatchRate: 94.6,
          missRate: '0.2%',
          avgDuration: '1.2s'
        },
        highRiskPatients: [
          { date: '2026-05-24', name: '方*辰', age: 14, clue: '铜蓝蛋白 0.055 g/L，ALT/AST 升高，疑似 K-F 环', aiSuggest: 'Wilson病 (极高危)' },
          { date: '2026-05-23', name: '何*澜', age: 6, clue: 'DBIL 68 μmol/L，TBA 180 μmol/L，GGT 不高', aiSuggest: 'PFIC2/ABCB11' },
          { date: '2026-05-22', name: '韩*一', age: 7, clue: '晨起低血糖 2.9 mmol/L，TG 与尿酸升高', aiSuggest: '糖原累积病I型/G6PC' },
          { date: '2026-05-21', name: '唐*曜', age: 16, clue: '非肥胖青少年 LDL-C 5.0 mmol/L，肝脾大', aiSuggest: 'LIPA缺乏症' }
        ]
      }
    }
  }
}

export const screeningDynamicHandlers = []

export const screeningRouteDocs = [
  {
    module: 'screening',
    method: 'GET',
    path: '/api/v1/web/screening/overview/',
    kind: 'exact',
    description: '筛查总览数据。'
  }
]
