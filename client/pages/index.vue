<template>
  <div class="page-default">
    <card-link
      to="/elden_ring"
      :subheader="$t('game.elden-ring')"
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
  }
})
export default class IndexPage extends Vue {

}
</script>

<style scoped>

</style>