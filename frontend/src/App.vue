
<template>
  <div class="app-layout">
    <el-menu mode="horizontal" router :default-active="$route.path">
      <el-menu-item index="/">
        <span>Smart Scheduler</span>
      </el-menu-item>
      <el-menu-item index="/">Dashboard</el-menu-item>
      <el-menu-item index="/setup">Setup</el-menu-item>
      <el-menu-item index="/admin">Admin</el-menu-item>
    </el-menu>
    <div class="main-content">
      <router-view />
    </div>
  </div>
</template>

<script setup lang="ts">
</script>

<style>
body {
  margin: 0;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
}
.main-content {
  padding: 20px;
}
</style>
