import axios from 'axios';

// 定义接口类型
export interface LoginParams {
  candidateNumber: string;
  idNumber: string;
}

export interface UserScore {
  candidateNumber: string;
  name: string; // 假设会返回姓名
  totalScore: number;
  politics: number;
  english: number;
  math: number;
  professional: number;
  rank?: number;
}

// 模拟后端接口请求
// 在实际项目中，这里会是 axios.post('/api/login', data)
export const loginAndGetScore = async (data: LoginParams): Promise<UserScore> => {
  console.log('正在请求登录接口...', data);
  
  // 模拟网络延迟 1.5s - 3s
  const delay = Math.random() * 1500 + 1500;
  
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      // 模拟简单的校验逻辑
      if (data.candidateNumber && data.idNumber) {
        // 模拟返回数据
        resolve({
          candidateNumber: data.candidateNumber,
          name: '张三',
          totalScore: 380,
          politics: 70,
          english: 75,
          math: 120,
          professional: 115,
          rank: 12
        });
      } else {
        reject(new Error('请输入有效的考生编号和证件号码'));
      }
    }, delay);
  });
};
