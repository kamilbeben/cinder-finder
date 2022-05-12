<template>
  <nuxt-link
    class="card-link"
    :to="to"
  >
    <v-card>
      <v-icon
        class="icon"
        v-text="icon"
      />
      <div
        class="subheader"
        v-text="subheader"
      />
    </v-card>
  </nuxt-link>
</template>

<script lang="ts">

import { Component, Prop, Vue } from 'nuxt-property-decorator'

@Component({
  name: 'CardLink'
})
export default class CardLink extends Vue {

  @Prop()
  private readonly icon !: string

  @Prop()
  private readonly subheader !: string

  @Prop()
  private readonly to !: string

}
</script>

<style scoped>

  .card-link {
    padding: 1em;
    text-decoration: none;
    flex: 1 1 auto;
    height: 0;
  }

  .card-link:not(:first-child) {
    margin-top: .75em;
  }

  .card-link > .v-card {
    display: flex;
    flex-direction: column;
    height: 100%;
  }

  .icon, .subheader {
    margin: .5rem auto;
  }

  .icon {
    font-size: 4em;
    margin-top: auto;
  }

  .subheader {
    font-size: 1.25em;
    margin-bottom: auto;
  }

</style>