/**
* 显示消息提示框
* @param content 提示的标题
*/
/*使用 uni.showToast 方法显示提示框。
icon: 'none'：表示不显示图标（仅显示文字）。
title: content：设置提示框的内容。*/

export function toast(content) {
  uni.showToast({
    icon: 'none',
    title: content
  })
}
/**
* 显示模态弹窗
* @param content 提示的标题
*/
export function showConfirm(content) {
  return new Promise((resolve, reject) => {
    uni.showModal({
      title: '提示',
      content: content,
      cancelText: '取消',
      confirmText: '确定',
      success: function(res) {
        resolve(res)
      }
    })
  })
}



/*
* 参数处理
* @param params 参数
*/

//这个函数用于将对象形式的参数转换为 URL 查询字符串
//
export function tansParams(params) {
  let result = ''
  for (const propName of Object.keys(params)) {
    const value = params[propName]
    var part = encodeURIComponent(propName) + "="
    if (value !== null && value !== "" && typeof (value) !== "undefined") {
      if (typeof value === 'object') {
        for (const key of Object.keys(value)) {
          if (value[key] !== null && value[key] !== "" && typeof (value[key]) !== 'undefined') {
            let params = propName + '[' + key + ']'
            var subPart = encodeURIComponent(params) + "="
            result += subPart + encodeURIComponent(value[key]) + "&"
          }
        }
      } else {
        result += part + encodeURIComponent(value) + "&"
      }
    }
  }
  return result
}
/*Object.keys(params): 获取 params 对象的所有键名。

encodeURIComponent: 对键名和键值进行 URL 编码，确保它们在 URL 中是安全的。

if (value !== null && value !== "" && typeof (value) !== "undefined"): 过滤掉空值或未定义的值。

if (typeof value === 'object'): 如果某个键的值是对象，则递归处理该对象的键值对。

result += part + encodeURIComponent(value) + "&": 将键值对拼接成 key=value& 的形式，并添加到结果字符串中。

return result: 返回最终的查询字符串。

使用场景: 当你需要将对象形式的参数转换为 URL 查询字符串时，可以使用这个函数。例如，发送 GET 请求时，将参数拼接到 URL 中。*/