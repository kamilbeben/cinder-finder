
import Vue from 'vue'
import { Component } from 'nuxt-property-decorator'

@Component
export default class WindowFocusAwareMixin extends Vue {

  protected onWindowFocus () : void | Promise<void>{
    console.error('WindowFocusAwareMixin :: onWindowFocus should be overridden')
  }

  protected beforeDestroy () : void {
    document.removeEventListener('window.onfocus', this.onWindowFocus)
  }

  protected mounted () : void {
    document.addEventListener('window.onfocus', this.onWindowFocus)
  }

}
