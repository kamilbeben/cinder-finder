<template>
  <nuxt-link
    class="card-link"
    :to="to"
    :data-selenium-id="seleniumId"
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

  @Prop(String)
  private readonly icon !: string

  @Prop(String)
  private readonly subheader !: string

  @Prop(String)
  private readonly to !: string

  @Prop({ type: String, required: true })
  private readonly seleniumId !: string

}
</script>

<style scoped>

  .card-link {
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
    background: #e5e5e564;
    /* box-shadow: none; */
  }

  .card-link:first-of-type > .v-card {
    border-radius: .5em .5em 0 0;
  }

  .card-link:last-of-type > .v-card {
    border-radius: 0 0 .5em .5em;
  }

  .icon {
    margin: auto;
    font-size: 4em;
    color: #1565C0;
  }

  .subheader {
    border-top: thin solid #e5e5e5;
    background: #e5e5e564;
    color: #344955;
    padding: 1rem;
    width: 100%;
    text-align: center;
    font-size: .9em;
  }

</style>