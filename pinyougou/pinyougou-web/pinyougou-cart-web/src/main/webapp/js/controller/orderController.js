// 定义订单控制器
app.controller('orderController', function ($scope, $controller, $interval, $location, baseService) {

    // 指定继承cartController
    $controller('cartController', {$scope:$scope});

    // 查询收件人地址列表
    $scope.findAddressByUser = function () {
        // 发送异步请求
        baseService.sendGet("/order/findAddressByUser").then(function (response) {
            // 获取响应数据 [{},{}] List<Address>
            $scope.addressList = response.data;

            // 获取默认地址
            $scope.address = $scope.addressList[0];

        });
    };

    // 用户选择收件地址
    $scope.selectAddress = function (item) {
        $scope.address = item;
    };

    // 判断地址是否选中，控制选中的样式
    $scope.isSelectedAddress = function (item) {
        return $scope.address == item;
    };


    // 定义数据封装的json对象
    $scope.order = {paymentType : '1'};

    /** 用户选择支付方式 */
    $scope.selectPayType = function (payType) {
        $scope.order.paymentType = payType;
    };


    // 提交订单
    $scope.submitOrder = function () {
        // 封装请求参数
        // 收货人地址
        $scope.order.receiverAreaName = $scope.address.address;
        // 收货人手机号码
        $scope.order.receiverMobile = $scope.address.mobile;
        // 收货人姓名
        $scope.order.receiver = $scope.address.contact;
        // 订单来源：pc端
        $scope.order.sourceType = "2";

        // 发送异步请求
        baseService.sendPost("/order/saveOrder",
            $scope.order).then(function(response){
            // 获取响应数据
            if (response.data){
                // 判断支付方式
                if ($scope.order.paymentType == "1"){
                    // 微信支付，跳转到支付页面
                    location.href = "/order/pay.html";
                }else{
                    // 货到付款，跳转到支付成功页面
                    location.href = "/order/paysuccess.html";
                }
            }else{
                alert("提交订单失败！");
            }
        });
    };


    // 生成微信支付二维码
    $scope.genPayCode = function () {
        // 发送异步请求
        baseService.sendGet("/order/genPayCode").then(function(response){
            // 获取响应数据 {outTradeNo : '', money : 200, codeUrl : ''}
            // 获取交易订单号
            $scope.outTradeNo = response.data.outTradeNo;
            // 获取交易金额
            $scope.money = (response.data.totalFee / 100).toFixed(2);
            // 获取微信支付URL
            $scope.codeUrl = response.data.codeUrl;

            // 生成二维码
            document.getElementById("qrious").src = "/barcode?url=" + $scope.codeUrl;

            /**
             * 开启定时器，间隔3秒发送异步请求
             * 第一个参数：回调函数
             * 第二个参数：间隔的时间 毫秒 3秒钟
             * 第三个参数：调用的总次数 100次
             */
            var timer = $interval(function(){
                // 发送异步请求
                baseService.sendGet("/order/queryPayStatus?outTradeNo="
                    + $scope.outTradeNo).then(function(response){
                    // 获取响应数据，判断支付状态 {status : 1|2|3} 1: 支付成功 2：未支付 3:支付失败
                    if (response.data.status == 1){
                        // 关闭定时器
                        $interval.cancel(timer);
                        // 支付成功，跳转到支付成功的页面
                        location.href = "/order/paysuccess.html?money=" + $scope.money;
                    }

                    if (response.data.status == 3){
                        // 关闭定时器
                        $interval.cancel(timer);
                        // 支付成功，跳转到支付失败的页面
                        location.href = "/order/payfail.html";
                    }
                });

            }, 3000, 100);

            // 总次数调用完成之后，才会调用then()
            timer.then(function(){
                // 调用关闭订单接口
                $scope.tip = "二维码已过期，请重新下单！";
            });

        });
    };

    // 获取URL中请求参数
    $scope.getMoney = function () {
        return $location.search().money;
    };

});