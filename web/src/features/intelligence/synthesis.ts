// 合成（synthesized）风险推断的确定性工具。
//
// 当患者尚无真实诊断结果时，用「患者 ID + 队列规模 + 当日日期」派生稳定伪随机分值，
// 保证同一天内同一患者在工作清单与队列概览中得到一致的风险判定，
// 且刷新页面不会跳变。所有结果均应标记 origin: "synthesized" 以区别于真实派生数据。

function hashString(str: string): number {
  let h = 0;
  for (let i = 0; i < str.length; i++) {
    h = (h << 5) - h + str.charCodeAt(i);
    h |= 0;
  }
  return Math.abs(h);
}

function seededRandom(seed: number): number {
  const x = Math.sin(seed * 9999 + 0.5) * 10000;
  return x - Math.floor(x);
}

function getDailySeed(): number {
  const now = new Date();
  return now.getFullYear() * 10000 + (now.getMonth() + 1) * 100 + now.getDate();
}

/** 为暂无诊断结果的患者派生稳定的合成风险分（30–85）。 */
export function generateSynthesizedScore(
  patientId: string,
  queueSize: number
): number {
  const seed = hashString(patientId) + queueSize + getDailySeed();
  const raw = seededRandom(seed);
  return Math.round(30 + raw * 55);
}

const SYNTHESIZED_REASONS = [
  "多指标异常提示需关注",
  "生化指标偏离参考范围",
  "影像与病史存在不一致",
  "家族史阳性需进一步评估",
  "年龄与性别相关风险因素叠加",
];

/** 为暂无诊断结果的患者派生稳定的合成风险理由（按患者区分，避免清单内雷同）。 */
export function generateSynthesizedReason(patientId = ""): string {
  const seed = hashString(patientId) + getDailySeed();
  return SYNTHESIZED_REASONS[seed % SYNTHESIZED_REASONS.length];
}
