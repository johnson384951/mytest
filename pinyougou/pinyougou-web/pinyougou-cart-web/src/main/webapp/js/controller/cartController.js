/** 定义购物车控制器 */
app.controller('cartController', function ($scope, $controller, baseService) {

    // 指定继承baseController
    $controller('baseController', {$scope : $scope});

    // 查询购物车
    $scope.findCart = function () {
        baseService.sendGet("/cart/findCart").then(function(response){
            // 获取响应数据
            $scope.carts = response.data;

            // 定义统计结果json对象
            $scope.totalEntity =  {totalNum : 0, totalMoney : 0, totalItem : 0};

            // 迭代用户的购物车集合
            for (var i = 0; i < $scope.carts.length; i++){
                // 获取一个数组元素
                var cart = $scope.carts[i]; // 商家的购物车

                // 统计商品数量
                $scope.totalEntity.totalItem += cart.orderItems.length;

                for (var j = 0; j < cart.orderItems.length; j++){
                    // 取一个数组元素
                    var orderItem = cart.orderItems[j];
                    // 统计购买的总件数
                    $scope.totalEntity.totalNum += orderItem.num;
                    // 统计购买的总金额
                    $scope.totalEntity.totalMoney += orderItem.totalFee;
                }
            }
        });
    };

    // 购物车加减、删除
    $scope.addCart = function (itemId, num) {
        baseService.sendGet("/cart/addCart?itemId="
            + itemId + "&num=" + num).then(function(response){
            // 获取响应数据
            if (response.data){
                // 重新查询购物车
                $scope.findCart();
            }else{
                alert("操作失败！");
            }
        });
    };
});