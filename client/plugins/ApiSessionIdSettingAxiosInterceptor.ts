
import { Context } from '@nuxt/types'
import { AxiosRequestConfig, AxiosResponse } from 'axios'

const SESSION_ID_HEADER_NAME = 'JSESSIONID'

// for fuck's sake, here's the deal:
// 1. server automatically creates a guest session if there is no JSESSIONID cookie set in the request
// 2. axios configuration as defined in 'nuxt.config.js' automatically sends JSESSION acquired by the client to the SSR context (but NOT vice versa)
// BUT! - requests fired on initial page load (there is no session avialable yet) does not share JSESSIONID, hence all this .:

// example.:
// - browser requests /elden_ring/room/1
//    - nuxt ssr requests /api/me
//        response's "set-cookie" header contains JSESSIONID -
//        that cookie's value is then assigned to `context.ssrContext.apiSessionId` (will be used by the next request) AND
//        set globally to the app using `context.app.$cookies` (will be picked up by the client)
//
//    - nuxt ssr requests /api/room/1
//        as i've said before, these requests do not share common cookies, so they need to be set manually - that's what's happening in onRequest
// - browser receives ssr response
//      thankfully, the cookie does not have to be handled manually at this point, because it had been set globally (see `context.app.$cookies.set`)
//      and will be send to the future ssr contexts by nuxt because of `nuxt.config.js`

export default function (context : Context) {

  context.$axios.onResponse((response : AxiosResponse) => {
    if (!process.server)
      return

    // example: 'JSESSIONID=8B62E8922BAFD743FA7E9ED08D304A32; Path=/; HttpOnly'
    const sessionIdHeaderLine =
      (<string[]> response.headers['set-cookie'] || [])
        .find(setCookieHeaderLine => setCookieHeaderLine.startsWith(SESSION_ID_HEADER_NAME))

    const sessionIdHeaderValue =
      sessionIdHeaderLine
        ? sessionIdHeaderLine!.split('=')[1]
        : null

    if (!sessionIdHeaderValue)
      return
    
    const sessionId = sessionIdHeaderValue.split(/;\s*/g)[0]

    // @ts-ignore
    context.ssrContext!.apiSessionId = sessionId
    context.app.$cookies.set(SESSION_ID_HEADER_NAME, sessionId)
  })

  context.$axios.onRequest((config: AxiosRequestConfig) => {

    if (!process.server)
      return config

    // @ts-ignore
    const ssrContextApiSessionId : string = context.ssrContext!.apiSessionId
    const appContextApiSessionId : string = context.app.$cookies.get(SESSION_ID_HEADER_NAME)

    if (ssrContextApiSessionId && ssrContextApiSessionId !== appContextApiSessionId) {      
      config.headers = config.headers || {}
      config.headers.Cookie =
        (
          config.headers.Cookie
            ? config.headers.Cookie + '; '
            : ''
        ) +
        SESSION_ID_HEADER_NAME + '=' + ssrContextApiSessionId
    }

    return config
  })

}
