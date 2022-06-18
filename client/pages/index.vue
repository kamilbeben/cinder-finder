<template>
  <div class="page-default">
    <card-link
      to="/elden_ring"
      :subheader="$t('game.elden-ring')"
      selenium-id="game-link.elden-ring"
    />
  </div>
</template>

<script lang="ts">

import { Component, Vue } from 'nuxt-property-decorator'

import CardLink from '~/components/CardLink.vue'

@Component({
  name: 'IndexPage',
  components: {
    CardLink
  },
  middleware: [
    function ({ redirect }) {
      redirect('/elden_ring')
    }
  ]
})
export default class IndexPage extends Vue {

}
</script>

<style scoped>

</style>