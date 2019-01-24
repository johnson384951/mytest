/** 定义控制器层 */
app.controller('userController', function($scope, $timeout, baseService){

    $scope.user = {};

    // 用户注册
    $scope.save = function () {
        // 判断两次密码是否致
        if (!$scope.password || $scope.password != $scope.user.password){
            alert("两次密码不一致！");
        }else{
            // 发送异步请求
            baseService.sendPost("/user/save?smsCode=" + $scope.smsCode, $scope.user)
                .then(function(response){
                    // 获取响应数据
                    if (response.data){
                        // 清空表单中数据
                        $scope.user = {};
                        $scope.password = "";
                        $scope.smsCode = "";
                        alert("注册成功");
                    }else{
                        alert("注册失败！");
                    }
            });
        }
    };

    // 发送短信验证码
    $scope.sendSmsCode = function () {

        // 判断手机号码是否正确
        if ($scope.user.phone && /^1[3|5|6|7|8]\d{9}$/.test($scope.user.phone)){
            // 发送异步请求
            baseService.sendGet("/user/sendSmsCode?phone="
                + $scope.user.phone).then(function(response){
                    // 获取响应数据
                    if (response.data){ // 发送成功
                        // 开启倒计时
                        $scope.downCount(90);
                    }else {
                        alert("短信发送失败！")
                    }
            });
        }else{
            alert("手机号码格式不正确！");
        }
    };

    // 定义提示文体
    $scope.tipText = "获取短信验证码";
    // 定义按钮是否可用变量
    $scope.disabled = false;
    // 定义倒计的方法
    $scope.downCount = function (seconds) {
        // 不断自减
        seconds--;
        if (seconds >= 0) {
            // 按钮禁用
            $scope.disabled = true;
            $scope.tipText = seconds + "S，后重新获取！"
            /**
             * 开启定时器
             * 第一个参数：需要定时调用的函数
             * 第二个参数：间隔的时间毫秒数
             */
            $timeout(function () {
                $scope.downCount(seconds);
            }, 1000);
        }else{
            // 启用按钮
            $scope.disabled = false;
            $scope.tipText = "获取短信验证码！"
        }
    };

});