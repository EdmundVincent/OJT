<template>
  <div class="ym-picker">
    <el-input-number v-model="start" :min="200000" :max="209912" placeholder="Start YYYYMM" />
    <span class="separator">~</span>
    <el-input-number v-model="end" :min="200000" :max="209912" placeholder="End YYYYMM" />
  </div>
</template>

<script setup>
import { defineModel } from 'vue'

const start = defineModel('start')
const end = defineModel('end')
</script>

<style scoped>
.ym-picker {
  display: flex;
  align-items: center;
}
.separator {
  margin: 0 10px;
}
</style>
