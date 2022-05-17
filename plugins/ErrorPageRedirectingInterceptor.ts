
import { Context } from '@nuxt/types'
import { AxiosError } from 'axios'

export default function (context : Context) {
  context.$axios.onResponseError((error : AxiosError<any>) => {
    console.log('ErrorPageREdirectingInterceptor # redirecting to /error', error)
    context.redirect('/error')
  })

}
