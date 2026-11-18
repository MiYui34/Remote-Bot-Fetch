// @ts-check
import { defineConfig } from "astro/config";

// 纯静态单页，没有集成、没有客户端脚本。
export default defineConfig({
  site: "https://rbf.sgu-server.com",
  build: {
    // 页面就一张，不用生成目录式路由
    format: "file",
  },
});
