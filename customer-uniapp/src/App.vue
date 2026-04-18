<script>
export default {
  onLaunch() {
    const token = uni.getStorageSync('token')
    if (!token) {
      uni.reLaunch({ url: '/pages/login/login' })
    }
  },
}
</script>

<style>
page { background: #f5f5f5; }
</style>
