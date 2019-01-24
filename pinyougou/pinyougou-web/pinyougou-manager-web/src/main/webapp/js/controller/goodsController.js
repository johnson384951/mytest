/** 定义控制器层 */
app.controller('goodsController', function($scope, $controller, baseService){

    /** 指定继承baseController */
    $controller('baseController',{$scope:$scope});

    /** 查询条件对象 */
    $scope.searchEntity = {};
    /** 分页查询(查询条件) */
    $scope.search = function(page, rows){
        baseService.findByPage("/goods/findByPage", page,
			rows, $scope.searchEntity)
            .then(function(response){
                /** 获取分页查询结果 */
                $scope.dataList = response.data.rows;
                /** 更新分页总记录数 */
                $scope.paginationConf.totalItems = response.data.total;
            });
    };

    // 定义状态码数组
    $scope.status = ["未审核", "已审核", "审核不通过", "关闭"];

    // 审核商品(修改商品的审核状态)
    $scope.updateStatus = function (status) {
        if ($scope.ids.length > 0){
            baseService.sendGet("/goods/updateStatus?ids="
                + $scope.ids + "&status=" + status).then(function (response) {
                // 获取响应数据
                if (response.data){
                    // 审核成功，重新加载数据
                    $scope.reload();
                    // 清空ids
                    $scope.ids = [];
                }else{
                    alert("操作失败！");
                }
            });
        }else {
            alert("请选择要审核的商品！");
        }
    };

    /** 批量删除(修改商品的删除状态) */
    $scope.delete = function(){
        if ($scope.ids.length > 0){
            baseService.sendGet("/goods/delete?ids="+ $scope.ids)
                .then(function(response){
                    if (response.data){
                        /** 重新加载数据 */
                        $scope.reload();
                        // 清空ids
                        $scope.ids = [];
                    }else{
                        alert("删除失败！");
                    }
                });
        }else{
            alert("请选择要删除的记录！");
        }
    };


});