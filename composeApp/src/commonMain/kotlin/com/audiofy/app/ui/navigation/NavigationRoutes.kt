package com.audiofy.app.ui.navigation

/**
 * 导航路由定义
 */
object NavigationRoutes {
    // 底部导航栏页面
    const val HOME = "home"           // 主页（阅读）
    const val LIBRARY = "library"     // 书架
    const val DISCOVER = "discover"   // 发现
    const val AUDIOBOOKS = "audiobooks" // 有声书
    const val PROFILE = "profile"     // 我的
    
    // 其他页面
    const val SETTINGS = "settings"                  // 设置
    const val CREATE_PODCAST = "create_podcast"      // 创建播客
    const val PODCAST_DETAIL = "podcast_detail/{podcastId}"  // 播客详情
    const val PLAYER = "player/{podcastId}"          // 全屏播放器
    const val PROCESSING = "processing"              // 生成进度
    const val READING = "reading/{podcastId}"        // 阅读页面
    
    // 带参数的路由构建器
    fun podcastDetail(podcastId: String) = "podcast_detail/$podcastId"
    fun player(podcastId: String) = "player/$podcastId"
    fun reading(podcastId: String) = "reading/$podcastId"
}

