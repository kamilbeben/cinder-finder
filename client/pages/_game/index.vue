<template>
  <div class="page-default">
    <card-link
      :to="`/${lowercaseGame}/create_room`"
      icon="mdi-account-multiple-plus"
      :subheader="$t('action.create-room')"
    />
    <card-link
      :to="`/${lowercaseGame}/rooms`"
      icon="mdi-format-list-bulleted"
      :subheader="$t('action.list-rooms')"
    />
  </div>
</template>

<script lang="ts">

import GameAwarePageMixin from '~/mixin/GameAwarePageMixin'
import { Component, mixins } from 'nuxt-property-decorator'

import CardLink from '~/components/CardLink.vue'

@Component({
  name: 'GameIndexPage',
  components: {
    CardLink
  }
})
export default class IndexPage extends mixins(GameAwarePageMixin) {

}
</script>

<style scoped>
  .page-default {
    flex: 1 1 auto;
  }
</style>
