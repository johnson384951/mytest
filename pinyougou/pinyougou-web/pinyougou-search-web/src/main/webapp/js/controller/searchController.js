/** 定义搜索控制器 */
app.controller("searchController" ,function ($scope, $sce, $location, baseService) {

    // 定义json对象封装查询条件
    $scope.searchParam = {keywords : '', category : '', brand : '',
        spec : {}, price : '', page : 1,
        sortField : '', sortValue : ''};

    // 搜索方法
    $scope.search = function () {
        baseService.sendPost("/Search", $scope.searchParam).then(function(response){
            // 获取响应数据 response.data: {rows : [{],{}], total : 100}
            $scope.resultMap = response.data;

            // 显示关键字变量
            $scope.keyword = $scope.searchParam.keywords;

            // 生成页码
            initPageNum();
        });
    };

    /** 把html格式的字符串转化成html标签 */
    $scope.trustHtml = function (html) {
        return $sce.trustAsHtml(html);
    };

    // 添加过滤条件
    $scope.addSearchItem = function (key, value) {
        // 判断是否为分类、品牌、价格
        if (key == 'category' || key == 'brand' || key == 'price'){
            $scope.searchParam[key] = value;
        }else{ // 规格选项
            $scope.searchParam.spec[key] = value; // 封装多个规格
        }
        // 执行搜索
        $scope.search();
    };

    // 删除过滤条件
    $scope.removeSearchItem = function (key) {
        // 判断是否为分类、品牌、价格
        if (key == 'category' || key == 'brand' || key == 'price'){
            $scope.searchParam[key] = '';
        }else{ // 规格选项
            // 删除json 对象中的key-value对象
            delete $scope.searchParam.spec[key];
        }
        // 执行搜索
        $scope.search();
    };


    /** 定义初始化页码方法 */
    var initPageNum = function(){
        /** 定义页码数组 */
        $scope.pageNums = [];
        /** 获取总页数 */
        var totalPages = $scope.resultMap.totalPages;
        /** 开始页码 */
        var firstPage = 1;
        /** 结束页码 */
        var lastPage = totalPages;

        // 前面是否加省略号
        $scope.firstDot = true;
        // 后面是否加省略号
        $scope.lastDot = true;

        /** 如果总页数大于5，显示部分页码 */
        if (totalPages > 5){
            // 如果当前页码处于前面位置
            if ($scope.searchParam.page <= 3){
                lastPage = 5; // 生成前5页页码
                $scope.firstDot = false;
            }
            // 如果当前页码处于后面位置
            else if ($scope.searchParam.page >= totalPages - 3){
                firstPage = totalPages - 4;  // 生成后5页页码
                $scope.lastDot = false;
            }else{ // 当前页码处于中间位置
                firstPage = $scope.searchParam.page - 2;
                lastPage = $scope.searchParam.page + 2;
            }
        }else{
            $scope.firstDot = false;
            $scope.lastDot = false;
        }

        /** 循环产生页码 */
        for (var i = firstPage; i <= lastPage; i++){
            $scope.pageNums.push(i);
        }
    };

    // 根据页码查询
    $scope.pageSearch = function (page) {

        //alert(typeof page);
        page = parseInt(page);

        // 判断当前页码的有效性: 页码不能小于1、页码不能大于总页数、页码不能等于当前页码
        if (page >= 1 && page <= $scope.resultMap.totalPages && page != $scope.searchParam.page){
            // 设置当前页码
            $scope.searchParam.page = page;
            // 执行搜索
            $scope.search();
        }
    };

    // 排序搜索
    $scope.sortSearch = function (key, value) {
        $scope.searchParam.sortField = key;
        $scope.searchParam.sortValue = value;
        // 执行搜索
        $scope.search();
    };

    // 获取首页传过来的关键字
    $scope.getKeywords = function () {
        // http://search.pinyougou.com/?keywords=小米&name=admin
        // 用$location服务中search()方法获取参数
        // $location.search() : http://search.pinyougou.com/?keywords=小米&name=admin {keywords : '', name : ''}
        var json = $location.search();

        $scope.searchParam.keywords = json.keywords;
        // 执行搜索
        $scope.search();

    };


});
