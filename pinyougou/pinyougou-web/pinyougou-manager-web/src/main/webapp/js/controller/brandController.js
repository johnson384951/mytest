// 模块添加控制器
app.controller('brandController', function ($scope, $controller, baseService) {

    // 指定brandController继承baseController
    $controller('baseController', {$scope : $scope});


    // 定义分页查询品牌的方法
    $scope.search = function (page, rows) {
        baseService.findByPage("/brand/findByPage", page,
            rows, $scope.searchEntity).then(function(response){
            // 获取响应数据 {rows : [{},{}], total : 100}
            // 分页品牌数据
            $scope.dataList = response.data.rows;
            // 更新总记录数
            $scope.paginationConf.totalItems = response.data.total;
        });
    };

    // 定义添加与修改品牌的方法
    $scope.saveOrUpdate = function () {
        // 定义添加URL
        var url = "save";
        if ($scope.entity.id){ // 修改
            url = "update";
        }
        // 发送异步请求
        baseService.sendPost("/brand/" + url,
            $scope.entity).then(function(response){
            // 获取响应数据 true | false
            if (response.data){
                // 重新查询数据
                $scope.reload();
            }else{
                alert("操作失败！");
            }
        });
    };

    // 为修改按钮绑定点击事件
    $scope.show = function (entity) {
        // 把entity 对象转化成一个新的json对象

        // 把json对象转化成json字符串
        var jsonStr = JSON.stringify(entity);
        // 把json字符串转化成json对象
        $scope.entity = JSON.parse(jsonStr);
    };


    // 为删除按钮绑定点击事件
    $scope.delete = function () {
        // 判断$scope.ids数组长度
        if ($scope.ids.length > 0){
            // 发送异步请求，批量删除品牌
            baseService.deleteById("/brand/delete", $scope.ids)
                .then(function(response){
                // 获取响应数据 true | false
                if (response.data){ // 删除成功
                    // 清空ids数组
                    $scope.ids = [];
                    // 重新加载数据
                    $scope.reload();
                }else{
                    alert("删除失败！");
                }
            });
        }else{
            alert("请选择要删除的品牌！");
        }
    };

});