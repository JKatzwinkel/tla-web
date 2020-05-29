$(window).on('pageshow', init);

// Sidebar 
 /*   $('html').not('#sidebar').click(function (e) {
		        //console.log($(e.target).parent());
		 if ($('#sidebar').is(':visible') && !e.target == '#sidebar') {
                $('#sidebar').toggle('slide', 
                    {direction: 'right'}, 
                    500);
            }
        });

    $('.sandwich').click(function (e) {
			e.preventDefault();
            $('#sidebar').toggle('fast');
        });
    $('.close-sidebar').click(function (e) {
			e.preventDefault();
            $('#sidebar').toggle('fast');
        });
*/


function copyStringToClipboard (str) {
       // Create new element
       var el = document.createElement('textarea');
       // Set value (string to be copied)
       el.value = str;
       // Set non-editable to avoid focus and move outside of view
       el.setAttribute('readonly', '');
       el.style = {position: 'absolute', left: '-9999px'};
       document.body.appendChild(el);
       // Select text inside element
       el.select();
       // Copy text to clipboard
       document.execCommand('copy');
       // Remove temporary element
       document.body.removeChild(el);
    }
	
function getCookie(Bezeichner) {
  var Wert = "";
  if (document.cookie) {
    var Wertstart = document.cookie.indexOf(Bezeichner+"=") + Bezeichner.length +1;
    var Wertende = document.cookie.indexOf(";", Wertstart);
    if (Wertende < Wertstart) {
      Wertende = document.cookie.length;
	}
	Wert = document.cookie.substring(Wertstart, Wertende);
  }
  return Wert;
}

function setCookie(Bezeichner, Wert) {
  var jetzt = new Date();
  var Auszeit = new Date(jetzt.getTime() + 1000 * 60 * 60 * 24 * 365);
  document.cookie = Bezeichner + "=" + Wert + "; expires=" + Auszeit.toGMTString() + "; path=/; samesite=lax";
}

function init() {	

    // Cookie Acceptance Banner ausblenden

	  var cookieAcceptanceState = getCookie("CookiePolicy");
	  if (cookieAcceptanceState == "accepted") {
		  $('.cookie-container').addClass('d-none');
		  var ausgabe = document.getElementById('cookie-info');
		  ausgabe.innerHTML = '(Cookies '+cookieAcceptanceState+')';
	  }
	
    $('.cookie-ok').click(function()  {
           $('.cookie-container').addClass('d-none');
            setCookie("CookiePolicy", "accepted");
            });
		
    $('.cookie-dismissed').click(function()  {
           $('.cookie-container').addClass('d-none');
            });

	// Search-Button
	
    $('.dictionary-search-form-btn').click(function()  {
		  var search_btn = document.getElementById("submit-search-form");
		  search_btn.innerHTML = '<span class="fas fa-arrow-circle-right"></span>Search in dictionary';
		  search_btn.setAttribute('form','dict-search')
		  });	

    $('.text-word-search-form-btn').click(function()  {
		  var search_btn = document.getElementById("submit-search-form");
		  search_btn.innerHTML = '<span class="fas fa-arrow-circle-right"></span>Search in texts';
		  search_btn.setAttribute('form','text-word-search')
		  });	
		  

	// Abbreviation help links
	
	$('.ling-glossing').click(function(e) {
			e.preventDefault();
			window.open('/help/ling-glossings','_blank');
		  });	

	$('.bts-glossing').click(function(e) {
			e.preventDefault();
			window.open('/help/bts-glossings','_blank');
		  });		
		  
		  
	// Search form settings		  

	// encoding radios
		if (getCookie("TransciptionEncoding") == "unicode") {
			$('#transcription_enc_unicode').prop("checked", true);
			$('#root_enc_unicode').prop("checked", true);
			}
		else {
			$('#transcription_enc_mdc').prop("checked", true);
			$('#root_enc_mdc').prop("checked", true);
			}

        $('#transcription_enc_unicode').click(function () {
			if (getCookie("CookiePolicy") == "accepted") {
				setCookie("TransciptionEncoding", "unicode");
				document.getElementById("root_enc_unicode").checked = true
			}
        });	
        $('#transcription_enc_mdc').click(function () {
			if (getCookie("CookiePolicy") == "accepted") {
				setCookie("TransciptionEncoding", "manuel_de_codage");
				document.getElementById("root_enc_mdc").checked = true
			}
        });	
        $('#root_enc_unicode').click(function () {
			if (getCookie("CookiePolicy") == "accepted") {
				setCookie("TransciptionEncoding", "unicode");
				document.getElementById("transcription_enc_unicode").checked = true
			}
        });	
        $('#root_enc_mdc').click(function () {
			if (getCookie("CookiePolicy") == "accepted") {
				setCookie("TransciptionEncoding", "manuel_de_codage");
				document.getElementById("transcription_enc_mdc").checked = true
			}
        });	

		
	// Show/Hide - Buttons
    
		// .sidebar
		$('html').not('#sidebar').click(function (e) {
		 if ($('#sidebar').is(':visible') && !e.target == '#sidebar') {
                $('#sidebar').slideUp('ease-out');
            }
        });
        $('.sidebar-btn, .close-sidebar').click(function (e) {
			e.preventDefault();
            $('#sidebar').slideToggle('slow');
        });	
    
		
  	// .bts-glossing
		if (getCookie("TLAFlexcodeVisible") == "true") {
			$('i', '.bts-glossing-btn').addClass("fa-plus-circle")
			$('.bts-glossing').show();
			}
		else {
			$('i', '.bts-glossing-btn').addClass("fa-minus-circle")
			$('.bts-glossing').hide();
			}

		$('html').not('.bts-glossing').click(function (e) {
		 if ($('.bts-glossing').is(':visible') && !e.target == '.bts-glossing') {
                $('.bts-glossing').slideUp('ease-out');
            }
        });
        $('.bts-glossing-btn').click(function (e) {
			e.preventDefault();
			if (getCookie("CookiePolicy") == "accepted") {
				if ($('.bts-glossing').is(':visible')) {
					setCookie("TLAFlexcodeVisible", "false");
				} else {
					setCookie("TLAFlexcodeVisible", "true");
				}
			}
            $('.bts-glossing').slideToggle('slow');
        });	
		
  	// .ling-glossing
		if (getCookie("LingGlossingVisible") == "true") {
			$('i', '.ling-glossing-btn').addClass("fa-plus-circle")
			$('.ling-glossing').show();
			}
		else {
			$('i', '.ling-glossing-btn').addClass("fa-minus-circle")
			$('.ling-glossing').hide();
			}

		$('html').not('.ling-glossing').click(function (e) {
		 if ($('.ling-glossing').is(':visible') && !e.target == '.ling-glossing') {
                $('.ling-glossing').slideUp('ease-out');
            }
        });
        $('.ling-glossing-btn').click(function (e) {
			e.preventDefault();
			if (getCookie("CookiePolicy") == "accepted") {
				if ($('.ling-glossing').is(':visible')) {
					setCookie("LingGlossingVisible", "false");
				} else {
					setCookie("LingGlossingVisible", "true");
				}
			}
            $('.ling-glossing').slideToggle('slow');
        });	
		
		
  	// .token-translation
		if (getCookie("TokenTranslationVisible") == "true") {
			$('i', '.token-translation-btn').addClass("fa-plus-circle")
			$('.token-translation').show();
			}
		else {
			$('i', '.token-translation-btn').addClass("fa-minus-circle")
			$('.token-translation').hide();
			}

		$('html').not('.token-translation').click(function (e) {
		 if ($('.token-translation').is(':visible') && !e.target == '.token-translation') {
                $('.token-translation').slideUp('ease-out');
            }
        });
        $('.token-translation-btn').click(function (e) {
			e.preventDefault();
			if (getCookie("CookiePolicy") == "accepted") {
				if ($('.token-translation').is(':visible')) {
					setCookie("TokenTranslationVisible", "false");
				} else {
					setCookie("TokenTranslationVisible", "true");
				}
			}
            $('.token-translation').slideToggle('slow');
        });	
		
  	// .text-date
		if (getCookie("TextDateVisible") == "false") {
			$('i', '.text-date-btn').addClass("fa-minus-circle")
			$('.text-date').hide();
			}
		else {
			$('i', '.text-date-btn').addClass("fa-plus-circle")
			$('.text-date').show();
			}

		$('html').not('.text-date').click(function (e) {
		 if ($('.text-date').is(':visible') && !e.target == '.text-date') {
                $('.text-date').slideUp('ease-out');
            }
        });
        $('.text-date-btn').click(function (e) {
			e.preventDefault();
			if (getCookie("CookiePolicy") == "accepted") {
				if ($('.text-date').is(':visible')) {
					setCookie("TextDateVisible", "false");
				} else {
					setCookie("TextDateVisible", "true");
				}
			}
            $('.text-date').slideToggle('slow');
        });	
		
  	// .text-editor
		if (getCookie("TextEditorVisible") == "false") {
			$('i', '.text-editor-btn').addClass("fa-minus-circle")
			$('.text-editor').hide();
			}
		else {
			$('i', '.text-editor-btn').addClass("fa-plus-circle")
			$('.text-editor').show();
			}

		$('html').not('.text-editor').click(function (e) {
		 if ($('.text-editor').is(':visible') && !e.target == '.text-editor') {
                $('.text-editor').slideUp('ease-out');
            }
        });
        $('.text-editor-btn').click(function (e) {
			e.preventDefault();
			if (getCookie("CookiePolicy") == "accepted") {
				if ($('.text-editor').is(':visible')) {
					setCookie("TextEditorVisible", "false");
				} else {
					setCookie("TextEditorVisible", "true");
				}
			}
            $('.text-editor').slideToggle('slow');
        });	
		


	// .combination-search
		$('html').not('.combination-search').click(function (e) {
		 if ($('.combination-search').is(':visible') && !e.target == '.combination-search') {
                $('.combination-search').slideUp('ease-out');
            }
        });
        $('.combination-search-btn').click(function (e) {
			e.preventDefault();
            $('.combination-search').slideToggle('slow');
        });	
    
     // .corpus-path
		$('html').not('.corpus-path-all').click(function (e) {
		 if ($('.corpus-path-all').is(':visible') && !e.target == '.corpus-path-all') {
                $('.corpus-path-all').slideUp('ease-out');
            }
        });
        $('.show-corpus-path').click(function (e) {
			e.preventDefault();
            $('.corpus-path-all, .hide-dots').slideToggle('slow');
        });	
    
        
    // .anno-block-btn
		if (getCookie("AnnotationBlockVisible") == "true") {
			$('i', '.anno-block-btn').addClass("fa-plus-circle")
			$('.container-annotation-switch-anno').show();
			$('.container-annotation-switch-lines').hide();
			$('.indented-buttons-annotation').show();
			}
		else {
			$('i', '.anno-block-btn').addClass("fa-minus-circle")
			$('.container-annotation-switch-anno').hide();
			$('.container-annotation-switch-lines').show();
			$('.indented-buttons-annotation').hide();
			}

		$('html').not('.anno-block-btn').click(function (e) {
		 if ($('.container-annotation-switch-anno').is(':visible') && !e.target == '.container-annotation-switch-anno') {
                $('.container-annotation-switch-anno').slideUp('ease-out');
            }
		 if ($('.indented-buttons-annotation').is(':visible') && !e.target == '.indented-buttons-annotation') {
                $('.indented-buttons-annotation').slideUp('ease-out');
            }
        });
        $('.anno-block-btn').click(function (e) {
			e.preventDefault();
			if (getCookie("CookiePolicy") == "accepted") {
				if ($('.container-annotation-switch-anno').is(':visible')) {
					setCookie("AnnotationBlockVisible", "false");
				} else {
					setCookie("AnnotationBlockVisible", "true");
				}
			}
            $('.container-annotation-switch-anno').slideToggle('slow');
            $('.container-annotation-switch-lines').slideToggle('slow');
            $('.indented-buttons-annotation').slideToggle('slow');
        });	
    

	// Show/Hide Comments
        
            $('.show-comment-button').click(function()  {    
             $('.comment-wrapper').toggleClass('hide-comment show-comment');             
            $('.show-comment-button').addClass('d-none');            
            $('.hide-comment-button').removeClass('d-none');
            
            });
        
    $('.hide-comment-button').click(function()  {
           $('.comment-wrapper').toggleClass('hide-comment show-comment');            
            $('.hide-comment-button').addClass('d-none');
            $('.show-comment-button').removeClass('d-none');
            });
        
		    
	// Headroom 
	    $(function() {
		var header = new Headroom(document.querySelector("#header"), {
			tolerance: 5,
			offset : 45,
			classes: {
			    // when element is initialised
				initial : "headroom",
				// when scrolling up
				pinned : "headroom--pinned",
				// when scrolling down
				unpinned : "headroom--unpinned",
				// when above offset
				top : "headroom--top"
			}
		});
		header.init();
		}());
		
		
	// change button-icons
	
    $(document).ready(function() {			
        $('.show-detail').click(function() {
    
        $("i", this).toggleClass("fas fa-plus-circle fas fa-minus-circle");
            });        	
			
		}); 
     
    // scrolltop
	
		$(document).ready(function(){
			$('.show-more').on('click', function(){
				$('html,body').animate({scrollTop: $(this).offset().top}, 800);
			}); 
		}); 

}

