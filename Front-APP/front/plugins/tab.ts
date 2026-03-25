const tab = {
  reLaunch(url: string): Promise<any> {
    return uni.reLaunch({
      url
    })
  },
  switchTab(url: string): Promise<any> {
    return uni.switchTab({
      url
    })
  },
  redirectTo(url: string): Promise<any> {
    return uni.redirectTo({
      url
    })
  },
  navigateTo(url: string): Promise<any> {
    return uni.navigateTo({
      url
    })
  },
  navigateBack(): Promise<any> {
    return uni.navigateBack()
  }
}

export default tab
