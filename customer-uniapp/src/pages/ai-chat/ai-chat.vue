<template>
  <view class="page">
    <!-- 消息列表 -->
    <scroll-view
      class="msg-list"
      scroll-y
      :scroll-top="scrollTop"
      scroll-with-animation
    >
      <!-- 欢迎语 -->
      <view class="msg-row ai">
        <view class="bubble ai-bubble">
          <text>你好！我是AI点餐助手，告诉我你想吃什么，我来帮你推荐 😊</text>
        </view>
      </view>

      <view
        v-for="(msg, idx) in messages"
        :key="idx"
        class="msg-row"
        :class="msg.role === 'user' ? 'user' : 'ai'"
      >
        <view class="bubble" :class="msg.role === 'user' ? 'user-bubble' : 'ai-bubble'">
          <text>{{ msg.content }}</text>
        </view>
      </view>

      <!-- 加载中气泡 -->
      <view v-if="thinking" class="msg-row ai">
        <view class="bubble ai-bubble thinking">
          <text>正在思考中...</text>
        </view>
      </view>
    </scroll-view>

    <!-- 输入区 -->
    <view class="input-bar">
      <input
        class="input"
        v-model="inputText"
        placeholder="说说你想吃什么..."
        :disabled="thinking"
        confirm-type="send"
        @confirm="send"
      />
      <view
        class="send-btn"
        :class="{ disabled: thinking || !inputText.trim() }"
        @click="send"
      >发送</view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { sendAiMessage } from '@/api/ai'

const messages = ref([])
const inputText = ref('')
const thinking = ref(false)
const scrollTop = ref(0)

async function send() {
  const text = inputText.value.trim()
  if (!text || thinking.value) return

  messages.value.push({ role: 'user', content: text })
  inputText.value = ''
  thinking.value = true
  scrollToBottom()

  try {
    const res = await sendAiMessage(text)
    messages.value.push({ role: 'ai', content: res.data })
  } catch (e) {
    messages.value.push({ role: 'ai', content: '抱歉，我遇到了一些问题，请稍后再试。' })
  } finally {
    thinking.value = false
    scrollToBottom()
  }
}

function scrollToBottom() {
  setTimeout(() => { scrollTop.value = 999999 }, 100)
}
</script>

<style scoped>
.page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: #f0f2f5;
}

.msg-list {
  flex: 1;
  padding: 24rpx 24rpx 0;
}

.msg-row {
  display: flex;
  margin-bottom: 24rpx;
}

.msg-row.user { justify-content: flex-end; }
.msg-row.ai   { justify-content: flex-start; }

.bubble {
  max-width: 70%;
  padding: 20rpx 28rpx;
  border-radius: 20rpx;
  font-size: 28rpx;
  line-height: 1.6;
  word-break: break-all;
}

.user-bubble {
  background: #FF6B35;
  color: #fff;
  border-bottom-right-radius: 4rpx;
}

.ai-bubble {
  background: #fff;
  color: #333;
  border-bottom-left-radius: 4rpx;
}

.thinking { color: #999; font-style: italic; }

.input-bar {
  display: flex;
  align-items: center;
  padding: 16rpx 24rpx;
  background: #fff;
  border-top: 1rpx solid #eee;
  gap: 16rpx;
}

.input {
  flex: 1;
  background: #f5f5f5;
  border-radius: 40rpx;
  padding: 18rpx 28rpx;
  font-size: 28rpx;
  color: #333;
}

.send-btn {
  background: #FF6B35;
  color: #fff;
  font-size: 28rpx;
  padding: 18rpx 36rpx;
  border-radius: 40rpx;
  flex-shrink: 0;
}

.send-btn.disabled { background: #ccc; }
</style>
