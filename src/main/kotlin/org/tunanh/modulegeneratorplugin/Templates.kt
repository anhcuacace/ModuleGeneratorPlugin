package org.tunanh.modulegeneratorplugin


object Templates {
    fun api(name: String, pkg: String) = """
        package $pkg

        import android.content.Context

        interface ${name}Api {
            fun getItems(): List<String>
            fun addItem(item: String): Boolean
            fun launchActivity(context: Context)
        }
    """.trimIndent()

    fun commonConfig(name: String, pkg: String) = """
        package $pkg

        interface ${name}CommonConfig {
            val languageCode: String
        }
    """.trimIndent()

    fun uiConfig(name: String, pkg: String) = """
        package $pkg

        interface ${name}UiConfig {
            val showAdvancedUI: Boolean
        }
    """.trimIndent()

    fun callbackConfig(name: String, pkg: String) = """
        package $pkg

        interface ${name}CallbackConfig {
            val onAction: ((action: String, data: Any?) -> Unit)?
                get() = null
        }
    """.trimIndent()

    fun moduleConfig(name: String, pkg: String) = """
        package $pkg

        interface ${name}ModuleConfig : 
            ${name}CommonConfig, 
            ${name}UiConfig, 
            ${name}CallbackConfig
    """.trimIndent()

    fun apiImpl(name: String, pkg: String) = """
        package $pkg

        import android.content.Context
        import android.content.Intent

        class ${name}ApiImpl(
            private val config: ${name}ModuleConfig,
            private val repo: ${name}Repository
        ) : ${name}Api {
            override fun getItems(): List<String> {
                config.onAction?.invoke("get_items", null)
                return repo.getItems()
            }

            override fun addItem(item: String): Boolean {
                config.onAction?.invoke("add_item", item)
                return repo.addItem(item)
            }

            override fun launchActivity(context: Context) {
                config.onAction?.invoke("launch_activity", config.languageCode)
                context.startActivity(Intent(context, ${name}Activity::class.java))
            }
        }
    """.trimIndent()

    fun entry(name: String, pkg: String) = """
        package $pkg

        import android.content.Context

        object ${name}ModuleEntry {
            @Volatile private var isInitialized = false
            private lateinit var api: ${name}Api

            fun initialize(context: Context, config: ${name}ModuleConfig) {
                if (isInitialized) return
                api = ${name}ApiImpl(config, ${name}Repository())
                isInitialized = true
            }

            fun getApi(): ${name}Api {
                check(isInitialized) { "${name}ModuleEntry must be initialized first!" }
                return api
            }
        }
    """.trimIndent()
}