// 查询列表页接口
const getOrderDetailPage = (params) => {
  return $axios({
    url: '/order/page',
    method: 'get',
    params
  })
}

// 查看接口
const queryOrderDetailById = (id) => {
  return $axios({
    url: `/orderDetail/${id}`,
    method: 'get'
  })
}

// 取消，派送，完成接口
const editOrderDetail = (params) => {
  return $axios({
    url: '/order',
    method: 'put',
    data: { ...params }
  })
}

// 统计分析-总营业额
const getStaticsTotal = () => {
  return $axios({
    url: '/statics/total',
    method: 'get'
  })
}

// 统计分析-菜系营收占比
const getDishCash = () => {
  return $axios({
    url: '/statics/dishCash',
    method: 'get'
  })
}

// 统计分析-套餐营收占比
const getSetmealCash = () => {
  return $axios({
    url: '/statics/setmealCash',
    method: 'get'
  })
}

// 统计分析-菜系销量占比
const getDishNum = () => {
  return $axios({
    url: '/statics/dishNum',
    method: 'get'
  })
}

// 统计分析-套餐销量占比
const getSetmealNum = () => {
  return $axios({
    url: '/statics/setmealNum',
    method: 'get'
  })
}

// 统计分析-近一年月营业额
const getCashPerMonth = () => {
  return $axios({
    url: '/statics/cashPerMonth',
    method: 'get'
  })
}
