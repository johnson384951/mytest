// 定义商品详情控制器
app.controller('itemController', function ($scope, $http) {

    // 购买数量加减操作
    $scope.addNum = function (x) {
        $scope.num += x;
        // 判断购买数时不能小于1
        if ($scope.num < 1){
            $scope.num = 1;
        }
    };

    // 失去焦点事件
    $scope.keyup = function () {
        if (isNaN($scope.num)){ // 不是数字
            $scope.num = 1;
        }else {
            $scope.num = parseInt($scope.num);
            // 判断购买数时不能小于1
            if ($scope.num < 1) {
                $scope.num = 1;
            }
        }
    };

    // 定义用户选中的规格对象
    // {"网络":"联通4G","机身内存":"64G"}
    $scope.spec = {};

    // 记录用户选择的规格选项
    $scope.selectedSpec = function (specName, optionName) {
        $scope.spec[specName] = optionName;
        // 根据用户选中的规格选项到 itemList SKU数组中 找对应的SKU
        $scope.searchSku();

    };

    // 用户选择的规格选项添加选中样式
    $scope.isSelected = function (specName, optionName) {
        return $scope.spec[specName] == optionName;
    };

    // 加载默认SKU，显示SKU标题与价格
    $scope.loadSku = function () {
        // 获取默认的SKU
        $scope.sku = itemList[0];
        // 规格选项
        $scope.spec = JSON.parse($scope.sku.spec);
    };

    // 搜索sku
    $scope.searchSku = function () {
        for (var i = 0; i <itemList.length; i++){
            // 获取数组中的元素 tb_item表中的一行数据
            var item = itemList[i];
            if (JSON.stringify($scope.spec) == item.spec){
                $scope.sku = item;
                break;
            }
        }
    };

    // 加入购物车按钮事件绑定
    $scope.addToCart = function () {

        // 发送异步请求(跨域请求) http://item.pinyougou.com 请求 http://cart.pinyougou.com
        $http.get("http://cart.pinyougou.com/cart/addCart?itemId="
            + $scope.sku.id + "&num=" + $scope.num, {withCredentials : true}).then(function(response){
                if (response.data){
                    // 跳转到购物车页面
                    location.href = "http://cart.pinyougou.com";
                }else{
                    alert("加入购物车失败！");
                }
        });
    };


});