const modal = {
  msg(content: string): void {
    uni.showToast({
      title: content,
      icon: 'none'
    })
  },
  msgError(content: string): void {
    uni.showToast({
      title: content,
      icon: 'error'
    })
  },
  msgSuccess(content: string): void {
    uni.showToast({
      title: content,
      icon: 'success'
    })
  },
  hideMsg(): void {
    uni.hideToast()
  },
  alert(content: string, title?: string): void {
    uni.showModal({
      title: title || '系统提示',
      content,
      showCancel: false
    })
  },
  confirm(content: string, title?: string): Promise<boolean> {
    return new Promise((resolve) => {
      uni.showModal({
        title: title || '系统提示',
        content,
        cancelText: '取消',
        confirmText: '确定',
        success(res: any) {
          resolve(!!res.confirm)
        }
      })
    })
  },
  showToast(option: Record<string, unknown> | string): void {
    if (typeof option === 'object') {
      uni.showToast(option)
      return
    }

    uni.showToast({
      title: option,
      icon: 'none',
      duration: 2500
    })
  },
  loading(content: string): void {
    uni.showLoading({
      title: content,
      icon: 'none'
    })
  },
  closeLoading(): void {
    uni.hideLoading()
  }
}

export default modal
