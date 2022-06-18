
import { Context } from '@nuxt/types'
import { AxiosRequestConfig, AxiosResponse } from 'axios'

export default function (context : Context) {

  context.$axios.onResponse((response : AxiosResponse) => {

    if (!process.client)
      return
    
    // @ts-ignore
    window.axiosResponses = window.axiosResponses || []
    // @ts-ignore
    window.axiosResponses.push(response)
  })

}
