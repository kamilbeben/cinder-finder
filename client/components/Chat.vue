<template>
  <div
    class="list"
    ref="scroll-container"
  >
    <div
      v-if="messages.length === 0"
      class="muted px-4 py-2"
      v-text="$t('room.messages-placeholder')"
    />
    <div
      v-for="(message, index) in messages"
      :key="index"
      class="message d-flex flex-column"
      data-selenium-id="chat.message"
    >

      <div class="d-flex my-auto py-1 header">
        <div
          class="in-game-name"
          :class="{
            'unset': !message.user.inGameName
          }"
          v-text="message.user.inGameName || $t('room.in-game-name-placeholder')"
        />
        <div
          class="muted ml-2 my-auto"
          v-text="`@${message.user.userName}`"
        />
        <div
          class="muted ml-auto"
          v-text="formatTimestamp(message.timestamp)"
        />
      </div>

      <div
        class="content"
        v-text="message.content"
      >
      </div>

    </div>

    <v-text-field
      class="reset-css message-input"
      v-model="messageContent"
      :rules="[
        RuleFactory.maxLength(250)
      ]"
      counter="250"
      append-outer-icon="mdi-send"
      data-selenium-id="chat.input"
      @click:append-outer="sendMessage"
      @keydown.enter="sendMessage"
    />
  </div>
</template>

<script lang="ts">

import { Component, Prop, Ref, Vue, Watch } from 'nuxt-property-decorator'
import RuleFactory from '~/service/RuleFactory'

import ChatMessage from '~/domain/ChatMessage'

// FIXME time zone differences between client and server
const MESSAGE_TIMESTAMP_FORMATTER = new Intl.DateTimeFormat('en-GB', { timeStyle: 'medium' });

@Component({
  name: 'Chat'
})
export default class Chat extends Vue {

  private readonly RuleFactory = new RuleFactory(this)

  @Prop(Array)
  private readonly messages !: ChatMessage[]

  @Prop(Number)
  private readonly roomId !: number[]

  @Ref('scroll-container')
  private scrollContainer !: HTMLElement

  private messageContent : string = ''
  private sendMessageActionIsLoading : boolean = false
  
  public async sendMessage () : Promise<void> {
    if (!this.messageContent || !this.messageContent.trim())
      return

    this.sendMessageActionIsLoading = true
    try {
      await this.$axios.post(
        `/api/room/message?id=${this.roomId}`,
        {
          content: this.messageContent.trim()
        })
      this.messageContent = ''
    } finally {
      this.sendMessageActionIsLoading = false
    }
  }

  private formatTimestamp (timestamp : number) : string {
    return MESSAGE_TIMESTAMP_FORMATTER.format(new Date(timestamp))
  }

  private get scrollBottomWatcherKey () : string {
    return JSON.stringify(this.messages)
  }

  @Watch('scrollBottomWatcherKey')
  private scrollBottom () : void {
    if (!this.scrollContainer) {
      this.$nextTick(() => this.scrollBottom())
      return
    }
    this.$nextTick(() => {
      const { scrollContainer } = this
      scrollContainer.scrollTop = scrollContainer.scrollHeight - scrollContainer.offsetHeight
      })
  }

  private mounted () : void {
    this.scrollBottom()
  }
}
</script>

<style scoped>

  .message {
    line-height: .9em;
    overflow: hidden;
    font-size: .9em;
  }

  .content {
    font-size: .9em;
    padding: .25em;
  }

</style>
