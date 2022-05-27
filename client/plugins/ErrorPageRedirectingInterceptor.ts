
import { Context } from '@nuxt/types'
import { AxiosError } from 'axios'

export default function (context : Context) {
  if (process.client)
    return

  context.$axios.onResponseError((error : AxiosError<any>) => {
    console.log('ErrorPageRedirectingInterceptor # redirecting to /error', error)
    context.redirect('/error')
  })

}
