/** 定义秒杀商品控制器 */
app.controller("seckillGoodsController", function($scope,$controller,$location,$timeout,baseService){

    /** 指定继承cartController */
    $controller("baseController", {$scope:$scope});

    /** 查询秒杀商品 */
    $scope.findSeckillGoodsList = function () {
        baseService.sendGet("/seckill/findSeckillGoodsList").then(function(response){
            // 获取响应数据
            $scope.seckillGoodsList = response.data;
        });
    };

    // 根据秒杀商品id查询秒杀商品对象
    $scope.findOne = function () {
        var id = $location.search().id;
        baseService.sendGet("/seckill/findOne?id=" + id).then(function(response){
            // 获取响应数据
            $scope.entity = response.data;


            // 调用倒计时方法
            $scope.downcount($scope.entity.endTime);
        });
    };

    /** 倒计时的方法 */
    $scope.downcount = function (endTime) {
        // 计算出结束时间与当前系统时间相差的毫秒数
        var millisSeconds = endTime - new Date().getTime();
        // 把相差的毫秒数转化成 小时:分:秒
        // 计算出相差的秒钟
        var seconds = Math.floor(millisSeconds / 1000);

        if (seconds >= 0) {
            // 计算出相差的分钟
            var minutes = Math.floor(seconds / 60);
            // 计算出相差的小时
            var hours = Math.floor(minutes / 60);
            // 计算出相差的天数
            var days = Math.floor(hours / 24);

            // 定义数组拼接时间字符串
            var res = new Array();

            if (days > 0) {
                res.push(calc(days) + "天 ");
            }
            if (hours > 0) {
                res.push(calc(hours - days * 24) + ":");
            }
            if (minutes > 0) {
                res.push(calc(minutes - hours * 60) + ":");
            }
            res.push(calc(seconds - minutes * 60));

            $scope.timeStr = res.join("");

            // 开启定时器
            $timeout(function () {
                $scope.downcount(endTime);
            }, 1000);
        }else{
            $scope.timeStr = "秒杀已结束！";
        }
    };

    var calc = function (num) {
        return num > 9 ? num : "0" + num;
    }


    // 立即抢购
    $scope.submitOrder = function () {
        // 判断用户是否登录
        if ($scope.loginName){ // 已登录
            // 秒杀下单
            baseService.sendPost("/order/saveOrder?id=" + $scope.entity.id).then(function(response){
                // 获取响应数据
                if (response.data){
                    // 秒杀成功，跳转到支付页面
                    location.href = "order/pay.html";
                }else{
                    alert("秒杀失败！");
                }
            });
        }else{ // 没有登录
            // 跳转到单点登录系统
            location.href = "http://sso.pinyougou.com/login?service=" + $scope.redirectURL;
        }
    };
    
});