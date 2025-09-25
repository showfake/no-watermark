import type { Data } from '../types/types';

const baseURL = import.meta.env.DEV ? 'local-api' : '/api'


export default function request<T>(uri: string, data?: object) {
  return new Promise<Data<T>>((resolve, reject) => {
    fetch(baseURL + uri, {
      method: data?'POST':'GET',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(data)
    })
    .then(res => res.json())
    .then(res => resolve(res))
    .catch(err => reject(err))
  })
}
