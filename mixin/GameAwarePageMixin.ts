
import Vue from 'vue'
import { Component } from 'nuxt-property-decorator'
import Game from '~/domain/Game'

@Component
export default class GameAwarePageMixin extends Vue {

  protected get game () : Game | null {
    const { game } = this.$route.params
    const gameParamIsCorrect = Object.values(Game).includes(<any> game.toUpperCase())
    return gameParamIsCorrect
      ? <Game> game.toUpperCase()
      : null
  }

  protected get lowercaseGame () : string {
    return (this.game as string)?.toLowerCase()
  }

  protected mounted () : void {
    if (!this.game)
      this.$router.push('/')

    // @ts-ignore
    window[this.$options.name] = this
  }

}
