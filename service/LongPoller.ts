
import { NuxtAxiosInstance } from '@nuxtjs/axios'
import { CancelTokenSource } from 'axios'
import Vue from 'vue'

export default class LongPoller<RESPONSE> {
  
  private $axios : NuxtAxiosInstance
  private endpoint : string
  private onResponse : (response : RESPONSE) => void
  private isRunning : boolean
  private requestCancelTokenSource : CancelTokenSource | null

  constructor ($axios : NuxtAxiosInstance, endpoint : string, onResponse : (response : RESPONSE) => void) {
    this.$axios = $axios
    this.endpoint = endpoint
    this.onResponse = onResponse
    this.isRunning = true
    this.requestCancelTokenSource = null

    this.subscribe()
  }

  public unsubscribe () : void {
    try {
      this.isRunning = false
      if (this.requestCancelTokenSource) {
        this.requestCancelTokenSource.cancel('Unsubscribed')
      }
    } catch (error) {
      console.error('LongPoller :: unsubscribe', error)
    }
  }

  private async subscribe () : Promise<void> {
    if (!this.isRunning)
      return

      
    try {
      this.requestCancelTokenSource = this.$axios.CancelToken.source()
      const response = await this.$axios.get<RESPONSE>(
        this.endpoint,
        {
          cancelToken: this.requestCancelTokenSource.token
        }
      )
      
      if (response.status === 200) {
        // move to the end of execution queue, let's create new request first
        setTimeout(() => this.onResponse(response.data), 0)
      } else if (response.status !== 502) {
        console.error('LongPoller :: got response', response)
      }
    } catch (error) {
      console.error('LongPoller :: subscribe', error)
    } finally {
      await this.subscribe()
    }
  }
}
