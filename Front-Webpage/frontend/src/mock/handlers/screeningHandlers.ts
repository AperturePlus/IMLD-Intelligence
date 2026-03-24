// @ts-nocheck

export const screeningExactHandlers = {
  'GET /api/v1/screening/overview/': async () => {
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
          { level: '极高', count: 85, percentage: 15, color: '#f56c6c' },
          { level: '高', count: 257, percentage: 35, color: '#e6a23c' },
          { level: '中', count: 420, percentage: 55, color: '#e6a23c' },
          { level: '低', count: 11688, percentage: 100, color: '#67c23a' }
        ],
        topGenes: [
          { name: 'ATP7B', desc: '肝豆状核变性', percentage: 82 },
          { name: 'HFE', desc: '遗传性血色病', percentage: 65 },
          { name: 'SERPINA1', desc: 'α1-抗胰蛋白酶缺乏', percentage: 48 },
          { name: 'SLC37A4', desc: '糖原累积病 Ib型', percentage: 30 },
          { name: 'UGT1A1', desc: 'Gilbert综合征', percentage: 22 },
          { name: 'G6PC', desc: '糖原累积病 Ia型', percentage: 15 },
          { name: 'NPC1', desc: '尼曼-匹克病', percentage: 8 }
        ],
        aiEfficiency: {
          diagnosisMatchRate: 94.6,
          missRate: '0.2%',
          avgDuration: '1.2s'
        },
        highRiskPatients: [
          { date: '2023-11-24', name: '李*豪', age: 14, clue: '铜蓝蛋白极低 (0.05 g/L)，AST/ALT 比例失调', aiSuggest: 'Wilson病 (极高危)' },
          { date: '2023-11-23', name: '赵*刚', age: 52, clue: '铁蛋白 > 1000 ng/mL，转铁蛋白饱和度 75%', aiSuggest: '遗传性血色病' },
          { date: '2023-11-22', name: '陈*明', age: 28, clue: '持续性非结合胆红素升高', aiSuggest: 'Gilbert综合征' },
          { date: '2023-11-21', name: '王*宇', age: 6, clue: '空腹低血糖伴乳酸酸中毒', aiSuggest: '糖原累积病' }
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
    path: '/api/v1/screening/overview/',
    kind: 'exact',
    description: '筛查总览数据。'
  }
]
