<template>
  <div class="ym-picker">
    <el-date-picker 
      v-model="start" 
      type="month" 
      value-format="YYYYMM" 
      placeholder="開始年月" 
      style="width: 140px" 
    />
    <span class="separator">~</span>
    <el-date-picker 
      v-model="end" 
      type="month" 
      value-format="YYYYMM" 
      placeholder="終了年月" 
      style="width: 140px" 
    />
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
