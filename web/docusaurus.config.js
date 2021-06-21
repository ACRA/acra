/** @type {import('@docusaurus/types').DocusaurusConfig} */
module.exports = {
  title: 'ACRA',
  tagline: 'Know your bugs.',
  url: 'https://www.acra.ch',
  baseUrl: '/',
  onBrokenLinks: 'throw',
  onBrokenMarkdownLinks: 'warn',
  trailingSlash: false,
  favicon: 'img/favicon.ico',
  organizationName: 'ACRA', // Usually your GitHub org/user name.
  projectName: 'acra', // Usually your repo name.
  themeConfig: {
    prism: {
      additionalLanguages: ['kotlin', 'groovy', 'java'],
    },
    navbar: {
      title: 'ACRA',
      logo: {
        alt: 'ACRA Logo',
        src: 'img/logo.png',
      },
      items: [
        {
          type: 'doc',
          docId: 'Setup',
          position: 'left',
          label: 'Documentation',
        },
        {to: 'pathname:///javadoc/latest', label: 'Dokka', position: 'left'},
        {
          href: 'https://github.com/ACRA/acra',
          label: 'GitHub',
          position: 'right',
        },
      ],
    },
    footer: {
      style: 'dark',
      links: [
        {
          title: 'Docs',
          items: [
            {
              label: 'Wiki',
              to: '/docs/Setup',
            },
          ],
        },
        {
          title: 'Community',
          items: [
            {
              label: 'Stack Overflow',
              href: 'https://stackoverflow.com/questions/tagged/acra',
            },
            {
              label: 'GitHub',
              href: 'https://github.com/ACRA/acra',
            },
          ],
        },
      ],
      copyright: `Copyright © ${new Date().getFullYear()} ACRA organization. Built with Docusaurus.`,
    },
  },
  presets: [
    [
      '@docusaurus/preset-classic',
      {
        docs: {
          sidebarPath: require.resolve('./sidebars.js'),
          // Please change this to your repo.
          editUrl: 'https://github.com/acra/acra/edit/master/web/',
        },
        theme: {
          customCss: require.resolve('./src/css/custom.css'),
        },
      },
    ],
  ],
};
