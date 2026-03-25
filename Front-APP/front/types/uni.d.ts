declare const uni: any
declare const wx: any
declare function require(path: string): any

declare module 'vue/types/vue' {
  interface Vue {
    $tab: any
    $auth: any
    $modal: any
    $store: any
    globalData: any
  }
}
