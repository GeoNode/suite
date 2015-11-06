// In case of static HTML, serve static info:

function onLoad() {
  var active_tab = $('#home');
  var previous_link;

  // set up link navigation
  function setUpTabsForIndex (tab_button) {
    $(tab_button).click(function(e) {
      e.preventDefault();
      active_tab.hide();

      switch (tab_button) {
        case '#gslink':
          active_tab = $('#gettingstarted');
          break;
        case '.homelink':
          active_tab = $('#home');
          break;
        case '.aboutlink':
          active_tab = $('#about');
          break;
        default:
          active_tab = $('#home');
          break;
      }
      active_tab.show();
    });

  }

  function setUpTabsForVersions(tab_button) {
    $(tab_button).click(function(e) {
      e.preventDefault();
      switch (tab_button) {
        case '#gslink':
          active_tab = '#gettingstarted';
          break;
        case '.homelink':
          active_tab = ' ';
          break;
        case '.aboutlink':
          active_tab = '#about';
          break;
        default:
          active_tab = null;
      }
      if (active_tab) {
        window.location.href = 'index.html' + active_tab;
      }
    });
  }

  var pagetitle = $(document).find("title").text();
  if (pagetitle.indexOf('Releases') > -1) {
    setUpTabsForVersions('#gslink');
    setUpTabsForVersions('.homelink');
    setUpTabsForVersions('.aboutlink');
  } else {
    setUpTabsForIndex('#gslink');
    setUpTabsForIndex('.homelink');
    setUpTabsForIndex('.aboutlink');
    var docs_link = $('#_documentationlink');
    docs_link.attr('href', '/opengeo-docs/');
  }

  $('.nav a').click(function(e) {
    if ($(this).attr("id") !== '_documentationlink') {
      e.preventDefault();
      $(this).tab('show');
    }
  });


  // set up getting started stepchoose-detail-inner show/hide
  function setUpStepDetails () {
    $(".data-link").click (function() {
      var target = $(this).attr('data-target');
      var dataclass = $(this).attr('dataclass');
      // hide all with that dataclass in title attribute
      var l = $('.stepchoose-detail-inner[dtitle="' + dataclass + '"]').hide();
      $('.stepchoose-detail[dtitle="' + dataclass + '"]').show();

      // show selected
      $(target).show();
    });
  }

  setUpStepDetails();

  // toggle accordion title active state
  $('#accordion h3.acc-toggle').click(function (e){
    $('#accordion h3.acc-toggle').removeClass("active");
    $(this).toggleClass("active");
  });

  // scroll to top of open accordion if in responsive view
  $(".panel-collapse").on("shown.bs.collapse", function () {
    if ($(window).width() <= 768) {
      var selected = $(this);

      $('html, body').animate({
          scrollTop: selected.offset().top - 70
      }, 500);
    }
  });

  // Add version info to all version spans
  var proj_version = $('.version').html();
  proj_version = '@suite_version@';
  $('.version').html(proj_version);

  // Add version info to all version spans
  var build_profile = '@build_profile@';
  if (build_profile==='ee') {
    var header = $('#header-tag');
    header.addClass('navbar-ee');
    $('.build_profile').show();
  } else {
    $('.build_profile').hide();
  }

  // Add version to all docs links
/*
  var docs_version = "docs/" + proj_version + "/";
  $('.docs').each(function( index ) {
      var component = $(this).attr("component");
      var path = this.href.split("#")[1];
      this.href = docs_version + path;
       this.target = '_blank';
    });
*/

  // Remove non-static info
  if ($('.commit').html() == " (${git.commit.id.abbrev})") {
    $('.commit').hide();
  }
  if ($('.date').html() == " built on ${build.prettydate}") {
    $('.date').hide();
  }
  $('.release_date').html('@build_shortdate@');

  // Initialize popovers
  $("#data-tip").popover();

}
