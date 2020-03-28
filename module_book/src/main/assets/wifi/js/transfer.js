$(function() {
	var isDragOver = false;
	
	var files = [];
	var progress;
	var currentFileName;

	var uploadQueue = [];
	var currentQueueIndex = 0;
	var isUploading = false;
	var getProgressReties = 0;
	
	var html5Uploader = null;
	var items = {};
	
	function initPageStrings() {
		document.title = STRINGS.WIFI_TRANS_TITLE;
		$('.content_title').text(STRINGS.FILES_ON_DEVICE);
		$('.table_header .filename').text(STRINGS.FILENAME);
		$('.table_header .size').text(STRINGS.FILE_SIZE);
		$('.table_header .operate').text(STRINGS.FILE_OPER);
	}
    
	function deleteBook(_event) {
		if (!confirm(STRINGS.CONFIRM_DELETE_BOOK)) {
			return;
		}
		var $node =  $(_event.currentTarget);
		var fileName = $node.siblings(':first').text();
		var fname = encodeURI(fileName);
		var fileInfoContainer = $node.parent();
		fileInfoContainer.css({ 'color':'#fff', 'background-color': '#cb4638' });
		fileInfoContainer.find('.trash').removeClass('trash').unbind();
		fileInfoContainer.find('.download').removeClass('download').unbind();
		$.post("deletefile", { 
            'fname': fileName
            }, function() {
			setTimeout(function() { 
				fileInfoContainer.slideUp('fast', function() {
					fileInfoContainer.remove();
				});
			}, 300);
		});
	}

	function loadFileList() {
		var now = new Date();
		var url = 'files_get?';
		$.getJSON(url+now, function(data) {
			files = data.fileList;
			var usrNameNode = $('.userName');
			usrNameNode.text(data.userName); 
			fillFilesContainer(); 
		});
	}

	function fillFilesContainer() {
		var height = $(window).height() - $("#right .content_title").height() - $("#right .table_header").height();
		var filesContainer = $("#right .files");
		filesContainer.empty();
		filesContainer.height(height);
		var rowsCount = Math.floor(height / 40);
	
		for (var i = 0; i < files.length;i ++) {
			var row = $('<div class="file"></div>');
			var fileInfo = files[i]; 
			row.append('<div class="column filename" filename="' + escape(fileInfo.name) + '">' + fileInfo.name +'</div>');
			row.append('<div class="column size">' + fileInfo.size + '</div>');
			row.append('<div class="column precent">上传成功</div>');
			row.append('<div class="column download" title="'+STRINGS.DOWNLOAD_FILE+'"></div>');
			row.append('<div class="column trash" title="'+STRINGS.DELETE_FILE+'"></div>');
			filesContainer.append(row);
		}
	
		return height;
	}
	function downloadBook(_event) {
		var $node =  $(_event.currentTarget);
		var fileName = $node.siblings(':first').text();
		var url = "/files_download?fname=" + fileName;
		window.location = url;
	}

	function getUploadProgress() {  
		$.getJSON('/doprogress?'+new Date(), {
            'fname': currentFileName
		},function(datas) {  
			progress = datas;
			fillProgressBar();
		});
	}
	
	function fillProgressBar() { 
			getProgressReties = 0;
			var ele = $("#right .file [filename='" + escape(progress.fileName) + "']")
			var eleSize = ele.next()
			eleSize.text(progress.size);
			var elePrecent = eleSize.next()
			elePrecent.text(Math.round(progress.progress * 100) + "%");
			var eleProgress = ele.prev();
			eleProgress.width(Math.round(progress.progress * 100)+'%');    

			if (progress.progress < 1) { 
				setTimeout(getUploadProgress, 300);
			} else { 
				elePrecent.text('上传成功');
			}  
	}
	
	function startAjaxUpload() {
		if (isUploading || currentQueueIndex >= uploadQueue.length) {
			return;
		}
		
		isUploading = true;
		var eleFile = $(uploadQueue[currentQueueIndex]);
		var eleFileId = eleFile.attr('id');
		var fileName = eleFile.val();
		var arr = fileName.split("\\");
		fileName = arr[arr.length - 1];
		
		currentQueueIndex ++;
		
		var row = $("#right .file [filename='" + escape(fileName) + "']").parent();
		
		$.ajaxFileUpload({
			url:'files',
			secureuri:false,
			fileElementId:eleFileId,
			dataType: 'text',
			success: function (data, status) {
				row.removeClass('progress_wrapper');
				row.find('.progress').remove();
				 
				$('<div class="column download" title="'+STRINGS.DOWNLOAD_FILE+'"></div>')
					//.click(downloadBook)
					.appendTo(row);
				$('<div class="column trash" title="'+STRINGS.DELETE_FILE+'"></div>')
					//.click(deleteBook)
					.appendTo(row);
				isUploading = false;
				
				//IE的诡异情况
				row.find('.download').text('');
				startAjaxUpload();
			},
			error: function (data, status, e) {
				isUploading = false;
				alert(STRINGS.UPLOAD_FAILED);
				row.remove();
				startAjaxUpload();
			}
		});
	
		currentFileName = fileName;
		setTimeout(getUploadProgress, 300);
	}
	
	function checkFileName(fileName) {
		if (!fileName || !fileName.toLowerCase().match('(epub|txt|umd)$')) {
			return STRINGS.UNSUPPORTED_FILE_TYPE;
		}
		
		var arr = fileName.split("\\");
		fileName = arr[arr.length - 1];
		
		var hasFile = false;
		var existFile = $("#right .file [filename='" + escape(fileName) + "']");
		if (existFile.length > 0) {
            $(this).val("");
			if (existFile.parent().hasClass('progress_wrapper')) {
				return STRINGS.FILE_IN_QUEUE;
			} else {
				return STRINGS.FILE_EXISTS;
			}
		}
		return null;
	}
	
	function uploadFiles(files) {
		var uploader = getHtml5Uploader();
		if (files.length == 1) {
			var msg = checkFileName(files[0].name || files[0].fileName);
			if (msg) {
				alert(msg);
				return;
			}
			uploader.add(files[0]);
			return;
		}
		
		var totalFiles = files.length;
		var actualFiles = 0;
        for (var i = 0; i < files.length; ++i) {
			if (!checkFileName(files[i].name || files[i].fileName)) {
				uploader.add(files[i]);
				actualFiles ++;
			}
        }
		if (totalFiles != actualFiles) {
			var msg = STRINGS.YOU_CHOOSE + totalFiles + STRINGS.CHOSEN_FILE_COUNT + actualFiles + STRINGS.VALID_CHOSEN_FILE_COUNT;
			alert(msg);
		}
	}

	function bindAjaxUpload(fileSelector) {
		$(fileSelector).unbind();
		$(fileSelector).change(function() {
			if (this.files) {
				uploadFiles(this.files)
				//优先使用HTML5上传方式
				return;
			}
			
			var fileName = $(this).val()
            
			var msg = checkFileName(fileName);
			if (msg) {
				alert(msg);
				return;
			}
                               
            var arr = fileName.split("\\");
            fileName = arr[arr.length - 1];
		
			var row = $('<div class="file progress_wrapper"></div>');
			row.append('<div class="progress"></div>');
			row.append('<div class="column filename" filename="' + escape(fileName) + '">' + fileName +'</div>');
			row.append('<div class="column size"> - </div>');
			row.append('<div class="column precent">0%</div>');
			$("#right .files").prepend(row);
			
			uploadQueue.push(fileSelector);
			$(fileSelector).css({ top: '-9999px', left: '-9999px' });
			$('.file_upload_warper').append('<input type="file" name="newfile" value="" id="newfile_' + uploadQueue.length + '" class="file_upload" />');
			bindAjaxUpload('#newfile_' + uploadQueue.length);
			startAjaxUpload();
		});
	}
	
	function formatFileSize(value) {
	    var multiplyFactor = 0;
	    var tokens = ["bytes","KB","MB","GB","TB"];
    
	    while (value > 1024) {
	        value /= 1024;
	        multiplyFactor++;
	    }
    
	    return value.toFixed(1) + " " + tokens[multiplyFactor];
	}
    
    function cancelUpload() {
        var uploader = getHtml5Uploader();
        var fileName = $(this).parent().find('.filename').attr('filename');
        if (fileName) {
            item = items[fileName];
            if (item) {
                uploader.abort(item);
            }
        }
    }
	
	function getHtml5Uploader() {
		if (!html5Uploader) {
			html5Uploader = new bitcandies.FileUploader({
				url: 'files',
				maxconnections: 1,
				fieldname: 'newfile',
                enqueued: function (item) {
					var fileName = item.getFilename();
                    items[escape(fileName)] = item;
					var size = item.getSize();
					var row = $('<div class="file progress_wrapper"></div>');
					row.append('<div class="progress"></div>');
					row.append('<div class="column filename" filename="' + escape(fileName) + '">' + fileName +'</div>');
					row.append('<div class="column size">' + formatFileSize(size) +'</div>');
					row.append('<div class="column precent">0%</div>');
					$("#right .files").prepend(row);
                },
                progress: function (item, loaded, total) {
					var fileName = item.getFilename();
					var progress = loaded / total;
					
					var ele = $("#right .file [filename='" + escape(fileName) + "']")
					var elePrecent = ele.next().next();
					elePrecent.text(Math.round(progress * 100) + "%");
					var eleProgress = ele.prev(); 
					eleProgress.width(Math.round(progress * 100)+'%'); 
                },
                success: function (item) {
					var fileName = item.getFilename();
					var row = $("#right .file [filename='" + escape(fileName) + "']").parent();
					
					row.removeClass('progress_wrapper');
					row.find('.progress').remove();
					row.find('.precent').text('上传成功');
					$('<div class="column download" title="'+STRINGS.DOWNLOAD_FILE+'"></div>').appendTo(row);
					$('<div class="column trash" title="'+STRINGS.DELETE_FILE+'"></div>').appendTo(row);

                },
                error: function (item) {
					var fileName = item.getFilename();
					
					var row = $("#right .file [filename='" + escape(fileName) + "']").parent();
					row.remove();
                },
                aborted: function (item) {
                    var fileName = item.getFilename();

					var row = $("#right .file [filename='" + escape(fileName) + "']").parent();
					row.remove();
                }
			});
		}
		return html5Uploader;
	}

	$(document).ready(function() {
		// events delegate
		$('.files').on('click', '.trash', deleteBook);
		$('.files').on('click', '.download', downloadBook);
		
		initPageStrings();
		loadFileList();
		$(window).resize(function() {
			fillFilesContainer();
		});
		bindAjaxUpload('#newfile_0');
		
	});
});